/*
 * ihome inc.
 * soc
 */
package com.ihome.soc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihome.soc.session.SocSession;

/**
 * 
 * @author sihai
 *
 */
public class SocResponse extends HttpServletResponseWrapper {

	private static final Log logger = LogFactory.getLog(SocResponse.class);
	
	private int     status;
	private boolean isWriterBuffered = true;
	private boolean flushed;
	
	private SocSession session;
	 
	private String sendRedirect;		// 
	private SendError sendError;		// 
	
	private BufferedServletOutputStream stream;	//
    private BufferedServletWriter writer;		//
    private PrintWriter streamAdapter;			//
    private ServletOutputStream writerAdapter;	//
	
	/**
     * 默认构造函数
     *
     * @param response
     */
    public SocResponse(HttpServletResponse response) {
        super(response);
        this.flushed = false;
    }
    
    /**
     * 设置isWriterBuffered模式，如果设置成<code>true</code>，表示将所有信息保存在内存中，否则直接输出到原始response中。
     * 
     * <p>
     * 此方法必须在<code>getOutputStream</code>和<code>getWriter</code>方法之前执行，否则将抛出<code>IllegalStateException</code>。
     * </p>
     *
     * @param isWriterBuffered 是否buffer内容
     *
     * @throws IllegalStateException <code>getOutputStream</code>或<code>getWriter</code>方法已经被执行
     */
    public void setWriterBuffered(boolean isWriterBuffered) {
    	if ((stream == null) && (writer == null)) {
    		if (this.isWriterBuffered != isWriterBuffered) {
    			this.isWriterBuffered = isWriterBuffered;
    			logger.debug("Set WriterBuffered " + (isWriterBuffered ? "on": "off"));
    		}
    	} else {
    		if (this.isWriterBuffered != isWriterBuffered) {
    			throw new IllegalStateException("Unable to change the isWriterBuffered mode since the getOutputStream() or getWriter() method has been called");
    		}
    	}
    }
    
    @Override
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * 
     */
    @Override
    public void sendRedirect(String location) {
    	 if((sendError == null) && (sendRedirect == null)) {
             sendRedirect = location;
         }
    	/*super.addHeader(SocConstants.LOCATION, location);
    	super.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);*/
    }
    
    @Override
    public void sendError(int status) throws IOException {
        sendError(status, null);
    }
    
    @Override
    public void sendError(int status, String message) throws IOException {
        if((sendError == null) && (sendRedirect == null)) {
            sendError = new SendError(status, message);
        }
        /*super.send
        super.setStatus(status);*/
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        
    	if (stream != null) {
            return stream;
        }

        if (writer != null) {
            // 如果getWriter方法已经被调用，则将writer转换成OutputStream
            // 这样做会增加少量额外的内存开销，但标准的servlet engine不会遇到这种情形，
            // 只有少数servlet engine需要这种做法（resin）。
            if (writerAdapter != null) {
                return writerAdapter;
            } else {
                logger.warn("Attampt to getOutputStream after calling getWriter.  This may cause unnecessary system cost.");
                writerAdapter = new WriterOutputStream(writer, getCharacterEncoding());
                return writerAdapter;
            }
        }
        if (this.isWriterBuffered) {
        	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            stream = new BufferedServletOutputStream(bytes);
            logger.debug("Created new byte buffer");
            return stream;
		} else {
			session.commit();
			return super.getOutputStream();
		}
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
		
    	if (writer != null) {
			return writer;
		}

		if (stream != null) {
			// 如果getOutputStream方法已经被调用，则将stream转换成PrintWriter。
			// 这样做会增加少量额外的内存开销，但标准的servlet engine不会遇到这种情形，
			// 只有少数servlet engine需要这种做法（resin）。
			if (streamAdapter != null) {
				return streamAdapter;
			} else {
				logger.warn("Attampt to getWriter after calling getOutputStream.  This may cause unnecessary system cost.");
				streamAdapter = new PrintWriter(new OutputStreamWriter(stream,getCharacterEncoding()), true);
				return streamAdapter;
			}
		}
		if (this.isWriterBuffered) {
			StringWriter chars = new StringWriter();
			writer = new BufferedServletWriter(chars);
			logger.debug("Created new character buffer");
			return writer;
		} else {
			session.commit();
			return super.getWriter();
		}
	}

    /**
     * 设置content长度。无效。
     *
     * @param length content长度
     */
    @Override
    public void setContentLength(int length) {
    	if(!isWriterBuffered)
          super.setContentLength(length);
    }
    
    /**
     * @wuyuan.lfk
     * 对writer、stream都进行判空，如果都为null，则执行父类的方法
     */
    @Override
    public void flushBuffer() throws IOException {
        
    	flushBufferAdapter();

        if (writer != null) {
            writer.flush();
        } else if (stream != null){
            stream.flush();
        } else{
        	super.flushBuffer();
        }

        this.flushed = true;
    }
    
    @Override
    public void resetBuffer() {
        
    	flushBufferAdapter();

        if (stream != null) {
            ((BufferedServletOutputStream) stream).updateOutputStream(new ByteArrayOutputStream());
        }

        if (writer != null) {
            ((BufferedServletWriter) writer).updateWriter(new StringWriter());
        }

        super.resetBuffer();
    }
    
    

    /**
     * 
     * @throws IOException
     */
    public void commit() throws IOException {
    	
    	 if (status > 0) {
             logger.debug("Set HTTP status to " + status);
             super.setStatus(status);
         }

         if (sendError != null) {
             if (sendError.message == null) {
                 logger.debug("Set error page: " + sendError.status);
                 super.sendError(sendError.status);
             } else {
                 logger.debug("Set error page: " + sendError.status + " " + sendError.message);
                 super.sendError(sendError.status, sendError.message);
             }
         } else if (sendRedirect != null) {
             logger.debug("Set redirect location to " + sendRedirect);
             super.sendRedirect(sendRedirect);
         } else if (stream != null) {
             flushBufferAdapter();
             OutputStream ostream = super.getOutputStream();
             ByteArray bytes = this.stream.getBytes().toByteArray();
             bytes.writeTo(ostream);
             logger.debug("Committed buffered bytes to the Servlet output stream");
         } else if (writer != null) {
            flushBufferAdapter();
            PrintWriter writer = super.getWriter();
 			try { 
 				String chars ;
 				chars = this.writer.getChars().toString();
 				writer.write(chars);
 			} catch (NullPointerException e) {
 				logger.debug(e + "write has been closed");
 			}
             
 			logger.debug("Committed buffered characters to the Servlet writer");
         }

         if (this.flushed) {
             super.flushBuffer();
         }
    }
    
    /**
     * 冲洗buffer adapter，确保adapter中的信息被写入buffer中。
     */
    private void flushBufferAdapter() {
        if (streamAdapter != null) {
            streamAdapter.flush();
        }

        if (writerAdapter != null) {
            try {
                writerAdapter.flush();
            } catch (IOException e) {
            	logger.error(e);
            }
        }
    }
    
    /**
     * 保存sendError的信息。
     */
    private static class SendError {
        
    	public final int status;
        public final String message;

        public SendError(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }
    
    /**
     * 将<code>Writer</code>适配到<code>ServletOutputStream</code>。
     */
    private static class WriterOutputStream extends ServletOutputStream {
        
    	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private Writer writer;
        private String charset;

        public WriterOutputStream(Writer writer, String charset) {
            this.writer = writer;
            this.charset = (null == charset ? "ISO-8859-1" : charset);
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write((byte) b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            buffer.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            ByteArray bytes = buffer.toByteArray();

            if (bytes.getLength() > 0) {
                ByteArrayInputStream inputBytes = new ByteArrayInputStream(bytes.getBytes(), bytes.getOffset(), bytes
                        .getLength());
                InputStreamReader reader = new InputStreamReader(inputBytes, charset);

                io(reader, writer);
                writer.flush();

                buffer.reset();
            }
        }

        @Override
        public void close() throws IOException {
            this.flush();
        }

        private void io(Reader in, Writer out) throws IOException {
            char[] buffer = new char[8192];
            int amount;

            while ((amount = in.read(buffer)) >= 0) {
                out.write(buffer, 0, amount);
            }
        }
    }

    
    /**
     * 代表一个将内容保存在内存中的<code>ServletOutputStream</code>。
     */
    private static class BufferedServletOutputStream extends ServletOutputStream {
        
    	private ByteArrayOutputStream bytes;

        public BufferedServletOutputStream(ByteArrayOutputStream bytes) {
            this.bytes = bytes;
        }

        public void updateOutputStream(ByteArrayOutputStream bytes) {
            this.bytes = bytes;
        }

        public ByteArrayOutputStream getBytes() {
            return this.bytes;
        }

        @Override
        public void write(int b) throws IOException {
            bytes.write((byte) b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            bytes.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            bytes.flush();
        }

        @Override
        public void close() throws IOException {
            bytes.flush();
            bytes.close();
        }
    }
    
    /**
     * 代表一个将内容保存在内存中的<code>PrintWriter</code>。
     */
    private static class BufferedServletWriter extends PrintWriter {
    	
    	public BufferedServletWriter(StringWriter chars) {
            super(chars);
        }

        public Writer getChars() {
            return this.out;
        }

        public void updateWriter(StringWriter chars) {
            this.out = chars;
        }
        
        public void close() {
        	try {
				this.out.close();
			} catch (IOException e) {
			}
        }
    }
    
    /**
     * 代表一个byte数组。
     *
     * @author Michael Zhou
     * @version $Id: ByteArray.java 593 2004-02-26 13:47:19Z baobao $
     */
    private static class ByteArray {
        private byte[] bytes;
        private int offset;
        private int length;

        public ByteArray(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public void writeTo(OutputStream out) throws IOException {
            out.write(bytes, offset, length);
        }
    }

    /**
     * 非同步的<code>ByteArrayOutputStream</code>替换方案, 执行<code>toByteArray()</code>
     * 方法时返回的是只读的内部字节数组, 避免了没有必要的字节复制. 本代码移植自IBM developer works精彩文章,
     * 参见package文档.
     *
     */
    private static class ByteArrayOutputStream extends OutputStream {
        private static final int DEFAULT_INITIAL_BUFFER_SIZE = 8192;

        // internal buffer
        private byte[] buffer;
        private int index;
        private int capacity;

        // is the stream closed?
        private boolean closed;

        // is the buffer shared?
        private boolean shared;

        public ByteArrayOutputStream() {
            this(DEFAULT_INITIAL_BUFFER_SIZE);
        }

        public ByteArrayOutputStream(int initialBufferSize) {
            capacity = initialBufferSize;
            buffer = new byte[capacity];
        }

        @Override
        public void write(int datum) throws IOException {
            if (closed) {
                throw new IOException("Stream closed");
            } else {
                if (index >= capacity) {
                    // expand the internal buffer
                    capacity = (capacity * 2) + 1;

                    byte[] tmp = new byte[capacity];

                    System.arraycopy(buffer, 0, tmp, 0, index);
                    buffer = tmp;

                    // the new buffer is not shared
                    shared = false;
                }

                // store the byte
                buffer[index++] = (byte) datum;
            }
        }

        @Override
        public void write(byte[] data, int offset, int length) throws IOException {
            if (data == null) {
                throw new NullPointerException();
            } else if ((offset < 0) || ((offset + length) > data.length) || (length < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (closed) {
                throw new IOException("Stream closed");
            } else {
                if ((index + length) > capacity) {
                    // expand the internal buffer
                    capacity = (capacity * 2) + length;

                    byte[] tmp = new byte[capacity];

                    System.arraycopy(buffer, 0, tmp, 0, index);
                    buffer = tmp;

                    // the new buffer is not shared
                    shared = false;
                }

                // copy in the subarray
                System.arraycopy(data, offset, buffer, index, length);
                index += length;
            }
        }

        @Override
        public void close() {
            closed = true;
        }

        public ByteArray toByteArray() {
            shared = true;
            return new ByteArray(buffer, 0, index);
        }

        public void reset() throws IOException {
            if (closed) {
                throw new IOException("Stream closed");
            } else {
                if (shared) {
                    // create a new buffer if it is shared
                    buffer = new byte[capacity];
                    shared = false;
                }

                // reset index
                index = 0;
            }
        }
    }
}
