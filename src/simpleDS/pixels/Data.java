/* Class for handling image-based data 
 * 
 * @history 15.Aug.2017 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 * @author <ahref="mailto:couly.guillaume@gmail.com">Guillaume Couly</a>
 * @author <ahref="mailto:clement.olalainty@gmail.com">Clement Olalanty</a>
 */

package simpleDS.pixels;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

public class Data {
	HashMap<String,ArrayList<BufferedImage>> data;
	HashMap<String,BufferedImage> dataset;
	HashMap<Integer,String> indexLabel;
	int imageWidth;
	int imageHeight;
	
	public Data (String folderName) {
		File rep = new File(folderName);
		data = new HashMap<>();
		dataset = new HashMap<>();
		indexLabel = new HashMap<>();
		
		for (File f : rep.listFiles()) {
			if (f.getName().endsWith(".png")) {
				String label = f.getName().split("_")[0];
				String imageID = f.getName().split("_")[1];

				if (!data.containsKey(label)) {
					data.put(label, new ArrayList<BufferedImage>());
				}

				try {
					BufferedImage bi;
					bi = ImageIO.read( f);
					imageWidth = bi.getWidth();
					imageHeight = bi.getHeight();
					data.get(label).add(bi);
					dataset.put(imageID, bi);

				} catch (IOException e) { 
					e.printStackTrace(); 
				}				
			}
		}
		
		int i = 0;
		for (String label : data.keySet()) {
			indexLabel.put(i , label);
			i++;
		}	
		
		for (String label : data.keySet()) {
			System.out.println( "label=" + label + " instances=" + data.get( label ).size());
		}
		
		for (int index : indexLabel.keySet()) {
			System.out.println( indexLabel.get(index) + " : " + index);
		}
		
		System.out.println("numlabels=" + getNumLabels());
		
	}
	
	public int getNumLabels() {
		return data.size();
	}

	public String getData (int index) {		
		String label = indexLabel.get( index);
		return getData(label, true);
	}
	
	public String getData (String label, boolean random) {
		BufferedImage bi;

		if (random) {
			ArrayList<BufferedImage> imageList = data.get( label);			
			Random r = new Random();
			int index = r.nextInt(imageList.size());
			bi = imageList.get(index);

		} else {
			bi = (BufferedImage) dataset.get(label+".png");
			if (bi == null) return null;
		}
		
		int counter = 0;
		int[] ret = new int[imageWidth*imageHeight];
		for (int i=0;i<imageHeight;i++) {
			for(int j=0;j<imageWidth;j++) {
				ret[counter] = (byte) Math.abs(bi.getRGB(j, i));
				counter++;
			}
		}
		
		String s = "";
		for (int b : ret) {
			s+=s.equals("") ? String.valueOf(b) : ","+String.valueOf(b);
		}
			
		return s;
		
	}
	
	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeigth() {
		return imageHeight;
	}
	
	public String getLabels(int index) {
		return indexLabel.get( index);
	}
}
