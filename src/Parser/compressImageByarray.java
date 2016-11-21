package Parser;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import Queue.QueuePutter;

public class compressImageByarray {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		byte[] content = QueuePutter.image2bytes("resource/pic/7.jpg");
		
	    Date begin = new Date();    
	    byte[]  imagecompressed=compress(content);
	    Date end = new Date();
	    
	    System.out.println(end.getTime() - begin.getTime());
 	}
	
	public static byte[] compress(byte[] data)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(data);  
		  
		BufferedImage src = null;  
		ByteArrayOutputStream out = null;  
		ImageWriter imgWrier;  
		ImageWriteParam imgWriteParams;  
		  
		// 指定写图片的方式为 jpg  
		imgWrier = ImageIO.getImageWritersByFormatName("jpg").next();  
		imgWriteParams = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(null);  
		// 要使用压缩，必须指定压缩方式为MODE_EXPLICIT  
		imgWriteParams.setCompressionMode(imgWriteParams.MODE_EXPLICIT);  
		// 这里指定压缩的程度，参数qality是取值0~1范围内，  
		imgWriteParams.setCompressionQuality((float)0.3);  
		                   
		imgWriteParams.setProgressiveMode(imgWriteParams.MODE_DISABLED);  
		ColorModel colorModel = ColorModel.getRGBdefault();  
		// 指定压缩时使用的色彩模式  
		imgWriteParams.setDestinationType(new javax.imageio.ImageTypeSpecifier(colorModel, colorModel  
		        .createCompatibleSampleModel(16, 16)));  
		  
		try  
		{  
		    src = ImageIO.read(is);  
		    out = new ByteArrayOutputStream(data.length);  
		  
		    imgWrier.reset();  
		    // 必须先指定 out值，才能调用write方法, ImageOutputStream可以通过任何 OutputStream构造  
		    imgWrier.setOutput(ImageIO.createImageOutputStream(out));  
		    // 调用write方法，就可以向输入流写图片  
		    imgWrier.write(null, new IIOImage(src, null, null), imgWriteParams);  
		      
		    out.flush();  
		    out.close();  
		    is.close();  
		    data = out.toByteArray();  
		}  
		catch(Exception e)  
		{  
		    e.printStackTrace();  
		}
		
		return data;
	}

}
