package cData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Web.convertXY;
import Incentives.newTasks;

public class XMLBuilder {
	public static void main(String[] args){
		List<Sitepixelxy> sitespixelList = selectPixel("/home/ps/ytt/yttps/resource/cData/beijing.xml", 2);
		for(Sitepixelxy sitepixelxy : sitespixelList){
			System.out.println(sitepixelxy.getPixelx());
		}
		
	}
	
	
	
	public static List<Sitepixelxy> selectPixel(String pathString,int zoom){
		List<Sitepixelxy> sitespixelList = new ArrayList<Sitepixelxy>();
		Sitepixelxy sitepixelxy = null;
		double[] pixelxy = new double[2];
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document xmlDocument = dBuilder.parse(new File(pathString));
			
			Element rootElement = xmlDocument.getDocumentElement();
			
			NodeList sites = rootElement.getChildNodes();
			int siteId = 0;
			for(int i = 0; i < sites.getLength(); i++){
				if("site".equals(sites.item(i).getNodeName())){
					siteId++;
					Element pixelx = (Element) selectSingleNode("/city/site[id='"+siteId+"']"+"/zoom"+ zoom+"/bpixelx", rootElement);
				
					pixelxy[0] = Double.valueOf(pixelx.getTextContent());
					Element pixely = (Element) selectSingleNode("/city/site[id='"+siteId+"']"+"/zoom"+ zoom+"/bpixely", rootElement);
					
					pixelxy[1] = Double.valueOf(pixely.getTextContent());
					sitepixelxy = new Sitepixelxy(siteId, pixelxy[0], pixelxy[1], zoom);
					
					//System.out.println(sitepixelxy.getPixelx() + "   " + sitepixelxy.getPixely());
					sitespixelList.add(sitepixelxy);
				}
				
			}
			
			
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sitespixelList;
	}
	
	
	public static void readXml(String pathString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element thElement = null, rootElement = null, thepixel = null;
		try{
			factory.setIgnoringElementContentWhitespace(true);
			
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document xmlDocument = dBuilder.parse(new File(pathString));
			rootElement = xmlDocument.getDocumentElement();
			
			NodeList sites = rootElement.getChildNodes();
			int siteslen = 0;
			
			for(int i = 0; i < sites.getLength(); i++){
				if("site".equals(sites.item(i).getNodeName())){
					//获取站点site的数目
					siteslen++;
				}
			}
			
			
			for(int i = 1; i <= siteslen;i++){
				Element siteElement = (Element) selectSingleNode("/city/site[id='"+i+"']", rootElement);
				double gpslat = 0; 
				double gpslon = 0;
				double[] baiduworldcor = new double[2];
				double[] baidupixel = new double[2];
				
				//得到站点的所有子节点
				NodeList sitedetail = siteElement.getChildNodes();
				for(int j = 0; j < sitedetail.getLength(); j++){
					Node detail = sitedetail.item(j);
					if("gpsLat".equals(detail.getNodeName())){
						gpslat = Double.valueOf(detail.getTextContent());
					}
					if("gpsLog".equals(detail.getNodeName())) {
						gpslon = Double.valueOf(detail.getTextContent());
					}
					
					
				}
				
				
				baiduworldcor = convertXY.gpstoworldcor(gpslon, gpslat);
				
				
				//增加每一个级别下的像素坐标
				for(int k = 1 ; k < 19; k++){
					thElement = xmlDocument.createElement("zoom" + k);
					thepixel = xmlDocument.createElement("bpixelx");
					baidupixel = convertXY.worldtopixel(baiduworldcor[0], baiduworldcor[1], k);
					
					thepixel.setTextContent(String.valueOf(baidupixel[0]));
					thElement.appendChild(thepixel);
					thepixel = xmlDocument.createElement("bpixely");
					thepixel.setTextContent(String.valueOf(baidupixel[1]));
					thElement.appendChild(thepixel);
					siteElement.appendChild(thElement);
				}
				
				
				output(xmlDocument);
				saveXml(pathString, xmlDocument);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	
	}
	
	
	public static void output(Node node){
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try{
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("encoding", "utf-8");
			transformer.setOutputProperty("indent", "yes");
			
			DOMSource source = new DOMSource();
			source.setNode(node);
			StreamResult result = new StreamResult();
			result.setOutputStream(System.out);
			
			transformer.transform(source, result);
			
		
		}catch(TransformerConfigurationException e){
			e.printStackTrace();
		}catch (TransformerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	
	public static Node selectSingleNode(String express, Object source){
		Node resultNode = null;
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		try{
			resultNode = (Node)xPath.evaluate(express, source, XPathConstants.NODE);
			
		}catch(XPathExpressionException e){
			e.printStackTrace();
		}
		return resultNode;
	}
	
	public static NodeList selectNodes(String express, Object source) {
		NodeList result = null;
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		try{
			result = (NodeList) xPath.evaluate(express, source, XPathConstants.NODESET);
			
		}catch(XPathExpressionException e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void saveXml(String fileNameString, Document doc) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");;
			
			Source xmlSource = new DOMSource(doc);
			Result result = new StreamResult(new File(fileNameString));
			
			transformer.transform(xmlSource, result);
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
}
