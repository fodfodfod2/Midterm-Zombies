import java.io.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.JFrame;


public class Main {
    public static void main(String[] args) {
    	int mapXSize = 200;
        int mapYSize = 200;
        int upscale = 1;
        double topographicalMapInterval = .5;
        double topographicTolerance = .05;
        
        int fallLineX = 120;
        int fallLineY = 125;
        
        int trailStartX = 100;
        int trailStartY = 100;
        
        double maxTrailAltitude = 2;
        double minTrailAltitude = .5;
        
        mapXSize *= upscale;
        mapYSize *= upscale;
        double[][] hills = new double[2][5];

        hills[0][0] = 50;  // xScale
        hills[0][1] = 30;   // yScale
        hills[0][2] = 50;  // x0
        hills[0][3] = 75;  // y0
        hills[0][4] = 2;   // zScale

        hills[1][0] = 60;
        hills[1][1] = 50;
        hills[1][2] = 150;
        hills[1][3] = 125;
        hills[1][4] = 3;

        double[][] map = new double[mapYSize][mapXSize];

        for (int y = 0; y < mapYSize; y++) {
            for (int x = 0; x < mapXSize; x++) {
                for (int idx = 0; idx < hills.length; idx++) {
                	
                	
                    double z_x = Math.pow((x/upscale - hills[idx][2]) / hills[idx][0], 2.0);
                    double z_y = Math.pow((y/upscale - hills[idx][3]) / hills[idx][1], 2.0);
                    map[y][x] += hills[idx][4] * Math.pow(Math.E, -(z_x + z_y));
                }
            }
        }
        Vector2[][] gradients = findGradients(map,hills,upscale);
        System.out.println("map built"); 
       

        try (FileWriter fw = new FileWriter("output/heightmap.csv")) {
            for (int y = 0; y < mapYSize; y++) {
                for (int x = 0; x < mapXSize; x++) {
                    fw.write(Double.toString(map[y][x]));
                    if (x < mapXSize - 1) fw.write(",");
                }
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) {
			e.printStackTrace();
		}

        
        
        
        // Find the peak
        Vector2[] peaks = new Vector2[hills.length];
        double maxVal = 0;
        int numOfFoundPeaks = 0;
        for (int y = 0; y < mapYSize; y++) {
            for (int x = 0; x < mapXSize; x++) {
            	if (isPeak(x,y,map)) {
            		Vector2 vec = new Vector2(x,y);
            		peaks[numOfFoundPeaks] = vec;
            		numOfFoundPeaks +=1;
            		if (map[y][x] > maxVal) {
            			maxVal = map[y][x];
            		}
            		System.out.println("Peak found at "+x+","+y);
            	}
            }
        }
        
        for (int y = 0; y < mapYSize; y++) {
            for (int x = 0; x < mapXSize; x++) {
            		if (map[y][x] > maxVal) {
            			maxVal = map[y][x];
            		}
            	
            }
        }
        
        int[][] topographicalMap = new int[mapYSize][mapXSize];
        

        for (int interval = 0; interval< maxVal/topographicalMapInterval;interval++) {
        	int xLoc = -5;
        	int yLoc = -5;
        	// find the closest point to the desired level curve val
        	for (int idx = 0; idx < peaks.length; idx++) {
        		int x = (int) peaks[idx].getX();
        		int y = (int) peaks[idx].getY();
        		if (Math.abs(gradients[y][x].getX()) < 1e-6 && Math.abs(gradients[y][x].getY()) < 1e-6) {
        		    // small random or fixed offset
        		    x += 1;
        		    y += 1;
        		}
        		double closest = 99999;
        		boolean inLoop = true;
        		yLoc = -5;
        		xLoc = -5;
	        	while (inLoop) {
	            	try {
	            	if (Math.abs(map[y][x] - interval*topographicalMapInterval) < closest) {
//	            		topographicalMap[y][x] = 4;
	            		closest = Math.abs(map[y][x] - interval*topographicalMapInterval);
	            		xLoc = x;
	            		yLoc = y;
	            	}
	            	else {
//	            		System.out.println("level point for hill "+idx+" and interval "+interval+" found at "+xLoc+","+yLoc+" with a value of "+closest);
//	            		System.out.println("escaped the level point search loop via passing the point");
	            		inLoop = false;
	            	}
	            	y -= gradients[y][x].getClosestOffset()[1];
	            	x -= gradients[y][x].getClosestOffset()[0];
	            	} catch (java.lang.ArrayIndexOutOfBoundsException e) {
//	            		System.out.println("escaped the level point search loop via array out of index");
	            		inLoop = false;
	            	}
	            }
	        	if (closest > .2) {
//	        		System.out.println("continued because the closest point was too far away");
	        		
	        	}else {
	        	// make the level curve
	        	
	        	//
	        	x = xLoc;
	        	y = yLoc;
	        	int xInit = x;
	        	int yInit = y;
	        	inLoop = true;
	        	int coef = 1;
	        	while (inLoop) {
	        		try {
	        			if (topographicalMap[y][x] != 1 || (x==xInit && y== yInit)) {
	        				topographicalMap[y][x] = 1;
//	        				System.out.println("did the thing at "+x+","+y);
//	        				inLoop = false;
	        			} else {
//	        				System.out.println("escaped the level curve creation loop via repeat value at "+x+","+y);
	        				inLoop = false;
	        			}
	        			// add perp to gradient
	        			int yZone = coef * -gradients[y][x].getClosestOffset()[0];
		            	int xZone = coef * gradients[y][x].getClosestOffset()[1];
		            	
		            	int xMin = 0;
		            	int yMin = 0;
//		            	y += gradients[y][x].getClosestOffset()[0];
//		            	x -= gradients[y][x].getClosestOffset()[1];
		            	if (xZone < 0) {
		            		xMin = xZone;
		            		xZone = 0;
		            	}
		            	if (yZone < 0) {
		            		yMin = yZone;
		            		yZone = 0;
		            	}
		            	
		            	closest = 99999;
		            	for (int xSearch = xMin; xSearch <= xZone; xSearch++) {
		            		for (int ySearch = yMin; ySearch <= yZone; ySearch++) {
		            			if (Math.abs(map[y+ySearch][x+xSearch] - interval*topographicalMapInterval) < closest && (xSearch != 0 || ySearch != 0)) {

		    	            		closest = Math.abs(map[y+ySearch][x+xSearch] - interval*topographicalMapInterval);
		    	            		xLoc = x+xSearch;
		    	            		yLoc = y+ySearch;
		    	            	}
		            			
		            		}
		            	}
		            	
		            	x = xLoc;
			        	y = yLoc;
//			        	topographicalMap[y][x] = 1;
	        		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
	        			
	        			if (coef == 1) {
	        				coef = -1;
	        				x = xInit;
	        	        	y = yInit;
	        	        	xLoc = xInit;
	        	        	yLoc = yInit;
//	        	        	System.out.println("retrying the level curve creation loop via array out of index at "+x+","+y);
	        			} else if (coef == -1) {
	        				inLoop = false;
//	        				System.out.println("escaped the level curve creation loop via array out of index at "+x+","+y);
	        			}
	        		}
	        		}
	        	}
	        	
	        	
        	}
	        
        }
        
        

//        for (int level = 1; level <= maxVal/topographicalMapInterval; level++) { 
//            double target = level * topographicalMapInterval;
//
//            for (int y = 1; y < mapYSize - 1; y++) {
//                for (int x = 1; x < mapXSize - 1; x++) {
//                    double val = map[y][x];
//
//                    // check if this pixel crosses the contour level
//                    boolean crosses =
//                        (val >= target - topographicTolerance && val <= target + topographicTolerance);
//
//                    if (crosses) {
//                        topographicalMap[y][x] = 1; // draw contour
//                    }
//                }
//            }
//        }
        
        
        
        
        
        
        int x = fallLineX;
        int y = fallLineY;
        // generate the fall line
        
        
        //go up
        while (true) {
        	try {
        	if (topographicalMap[y][x] == 2) {
        		break;
        	}
        	topographicalMap[y][x] = 2;
        	
        	y += gradients[y][x].getClosestOffset()[1];
        	x += gradients[y][x].getClosestOffset()[0];
        	} catch (java.lang.ArrayIndexOutOfBoundsException e) {
        		break;
        	}
        }
        
        x = fallLineX;
        y = fallLineY;
        // go down
        while (true) {
        	try {
        	if (topographicalMap[y][x] == 3 && x != fallLineX && y != fallLineY) {
        		break;
        	}
        	topographicalMap[y][x] = 2;
        	
        	y -= gradients[y][x].getClosestOffset()[1];
        	x -= gradients[y][x].getClosestOffset()[0];
        	} catch (java.lang.ArrayIndexOutOfBoundsException e) {
        		break;
        	}
        }
        
        x = trailStartX;
        y = trailStartY;
        boolean inLoop = true;
        int coef = 1;
        boolean reset = false;
        Vector2 trailEnd = new Vector2();
        
        int lastX = -1;
        int lastY = -1;
        
        double lastZ = -1;
        
        while (inLoop) {
        	try {
        		

        		
        		
        		
            	if (((topographicalMap[y][x] != 5 && topographicalMap[y][x] != 4 && topographicalMap[y][x] != 6) || (x == trailStartX && y == trailStartY) ) || reset) {
            		
            		if (lastX == -1) {
            			topographicalMap[y][x] = 6;
            		} else if (   Math.abs((map[y][x]-lastZ)/Math.sqrt(Math.pow(y-lastY,2)+Math.pow(x-lastX,2)    ))  < .2) {
            			topographicalMap[y][x] = 4;
            		} else {
            			topographicalMap[y][x] = 5;
            		}
            		System.out.println(Math.abs((map[y][x]-lastZ)/Math.sqrt(Math.pow(y-lastY,2)+Math.pow(y-lastY,2)) ));
        			System.out.println(Math.sqrt(Math.pow(y-lastY,2)+Math.pow(x-lastX,2)));
        			System.out.println(Math.pow(y-lastY,2));
        			System.out.println(lastY);
            		
            		lastX = x;
            		lastY = y;
            		lastZ = map[y][x];
            			
            		
//            		System.out.println("making path at "+x+","+y);
            	} else {
            		System.out.println("breaking trail loop due to repeat val at "+x+","+y);
            		map[mapYSize][mapXSize] = .1; // this is just going to error
            	}
            	
            	
            	
            	
            	int tempY;
            	int tempX;
            	if (!reset) {
            	if (coef * gradients[y][x].getClosestOffset()[0] == 0) {
	            	tempY = (coef * gradients[y][x].getClosestOffset()[1]);
	            	tempX = (coef * gradients[y][x].getClosestOffset()[0]+2)%3 -1;
            	} else if (coef * gradients[y][x].getClosestOffset()[1] == 0) {
            		tempY = (coef * gradients[y][x].getClosestOffset()[1]+2)%3 -1;
            		tempX = (coef * gradients[y][x].getClosestOffset()[0]);
            	} else {
            		tempY = (coef * gradients[y][x].getClosestOffset()[1]+2)%3 -1;
            		tempX = (coef * gradients[y][x].getClosestOffset()[0]);
            	}
            	System.out.println("moving by "+tempX+","+tempY+"at "+x+","+y);
            	y+=tempY;
            	x+=tempX;
            	
            	if (map[y][x] > maxTrailAltitude ||  map[y][x] < minTrailAltitude) {
            		coef = -coef;
            	}
            	} else if (reset) {
            		if (x != trailEnd.getX() || y != trailEnd.getY()) {
            			if (Math.abs((map[y][x]-lastZ)/Math.sqrt(Math.pow(y-lastY,2)+Math.pow(x-lastX,2)) )  < .2) {
                			topographicalMap[y][x] = 4;
                		} else {
                			topographicalMap[y][x] = 5;
                		}
            			
            			
            			
            			
            			lastX = x;
                		lastY = y;
                		lastZ = map[y][x];
            			
                	} else {
                		System.out.println("im free");
                		inLoop = false;
                	}
            		
            		
            		Vector2[] possibleMoves = new Vector2[4];
            		possibleMoves[0] = new Vector2();
            		possibleMoves[1] = new Vector2();
            		possibleMoves[2] = new Vector2();
            		possibleMoves[3] = new Vector2();
            		
            		int[] grad = gradients[y][x].getClosestOffset();
            		
            		if (grad[0] != 0 && grad[1] != 0) {
            			
            			possibleMoves[0].setX(-1);
            			possibleMoves[0].setY(0);
            			possibleMoves[1].setX(1);
            			possibleMoves[1].setY(0);
            			
            			possibleMoves[2].setX(0);
            			possibleMoves[2].setY(-1);
            			possibleMoves[3].setX(0);
            			possibleMoves[3].setY(1);
            			
            		} else if (grad[0] == 0 || grad[1] == 0) {
            			
            			possibleMoves[0].setX(-1);
            			possibleMoves[0].setY(1);
            			possibleMoves[1].setX(1);
            			possibleMoves[1].setY(-1);
            			
            			possibleMoves[2].setX(-1);
            			possibleMoves[2].setY(-1);
            			possibleMoves[3].setX(1);
            			possibleMoves[3].setY(1);
            		} 
            		
            		double closest = 9999;
            		int closestIDX = -1;
            		for (int idx = 0; idx < possibleMoves.length; idx++) {
            			double distance = Math.sqrt(Math.pow(  (x+possibleMoves[idx].getX() ) - trailEnd.getX(),2) + Math.pow((y+possibleMoves[idx].getY() ) - trailEnd.getY(),2));
            			if (distance < closest) {
            				closest = distance;
            				closestIDX = idx;
            				
            						
            			}
            			
            			
            		}
            		System.out.println("moving by "+possibleMoves[closestIDX].getX()+","+possibleMoves[closestIDX].getY()+"at "+x+","+y);
                	y+=possibleMoves[closestIDX].getY();
                	x+=possibleMoves[closestIDX].getX();
            		
            	}
            	
            	
        	} catch (java.lang.ArrayIndexOutOfBoundsException e) {
        		if (!reset) {
	        		coef = -1;
	        		trailEnd.setX(x);
	        		trailEnd.setY(y);
	        		x = trailStartX;
	                y = trailStartY;
	                System.out.println("repeating trail loop due to index error at "+x+","+y);
	                reset = true;
//	                inLoop = false;
        		} else {
        			inLoop = false;
	        		System.out.println("breaking trail loop due to index error at "+x+","+y);
        		}
        	}
        }
        
        
        
        
        
        
        chartStuff(topographicalMap,3);


    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void chartStuff(int[][] topographicalMap, int pixelSize) {
    	int PIXEL_SIZE = pixelSize;
        JFrame frame = new JFrame("Map");
        PixelGrid panel = new PixelGrid(topographicalMap, PIXEL_SIZE);
        
        int width = topographicalMap[0].length * PIXEL_SIZE;
        int height = topographicalMap.length * PIXEL_SIZE;
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width + 16, height + 39); 
        frame.add(panel);
        frame.setVisible(true);
        
        
        runPython();
    }
    
    public static void runPython() {
        try {
            // 1. Make sure your map is saved to heightmap.csv first
            String csvPath = "output/heightmap.csv";

            // 2. Build the Python process command
            ProcessBuilder pb = new ProcessBuilder("python3", "python/plot.py", csvPath);

            // 3. Redirect error & output
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 4. Read Python output (optional)
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Vector2[][] findGradients(double[][] map, double[][] hills, int upscale) {
        Vector2[][] ans = new Vector2[map.length][map[0].length];

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Vector2 gradient = new Vector2(0, 0);
                for (int idx = 0; idx < hills.length; idx++) {
                    double xRel = ((double)x / upscale - hills[idx][2]) / hills[idx][0];
                    double yRel = ((double)y / upscale - hills[idx][3]) / hills[idx][1];
                    double expTerm = Math.exp(-(xRel * xRel + yRel * yRel));

                   
                    double dx = -2 * hills[idx][4] * xRel / hills[idx][0] * expTerm;
                    double dy = -2 * hills[idx][4] * yRel / hills[idx][1] * expTerm;

                    gradient.addX(dx);
                    gradient.addY(dy);
                }
                ans[y][x] = gradient;
            }
        }

        System.out.println("Gradient at 50,75 is " +ans[75][50].getX() + "i + " + ans[75][50].getY() + "j");
        return ans;
    }
    public static boolean isPeak(int x, int y, double[][] map) {
    	int xmin = -1;
    	int xmax = 1;
    	int ymin = -1;
    	int ymax = 1;
    	if (x==0) {
    		xmin = 0;
    	}
    	if (x==map[0].length-1) {
    		xmax = 0;
    	}
    	if (y==0) {
    		ymin = 0;
    	}
    	if (y==map.length-1) {
    		ymax = 0;
    	}
//    	System.out.println("finding peaks at "+x+","+y+" with x varying from "+xmin+" to "+xmax+" and with y varying from "+ymin+" to "+ymax);
    	for (int yoffset = ymin; yoffset<=ymax; yoffset+=1) {
    		for (int xoffset = xmin; xoffset<=xmax; xoffset+=1) {
        		if (map[y+yoffset][x+xoffset] > map[y][x]) {
        			return false;
        		}
        	}
    	}
    	return true;
    }
    
   
}
