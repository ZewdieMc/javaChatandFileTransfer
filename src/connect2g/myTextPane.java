/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connect2g;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JTextPane;


public class myTextPane extends JTextPane{

    public myTextPane() {
        setOpaque(false);
        
    }
     @Override
    protected void paintComponent(Graphics g) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/chat.jpg"));
            g.drawImage(image, 0, 0, (int) getSize().getWidth(),
                    (int) getSize().getHeight(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.paintComponent(g);
    }
    
}
