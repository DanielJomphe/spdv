package spdv.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

/**
 * Original code came public domain from Data Virtue:
 * http://www.datavirtue.com/Software/dev/LinePrinter.java
 * Sorry for any spelling mistakes, if you improve this class PLEASE send me the new code!
 * seanka@datavirtue.com
 * <p/>
 * This class simulates an old style (dot matrix, daisy wheel) printer.
 * Just feed it the Font and some text: addLine(String), newLine(), formFeed(), and it shoots out perfectly on the paper
 * No line wrapping, make sure to split up strings that contain new lines if you want the new lines to "print".
 * <p/>
 * If 'boolean prompt' is set to false no print dialog is shown and printing takes place immediately.
 * This class still needs optimized for memory, you must always step lightly when printing large amount of data/pages.
 * <p/>
 * This class could stand to be modified to allow greater control and configuration of the rendering page.
 * <p/>
 * see main() for an example, just compile and run this class to see it in action.  A print dialog will appear.
 */
public class LinePrinter {

    /* instance vars */
    private PageFormat pgFormat = new PageFormat();
    private Book book = new Book();
    private Paper p;


    private boolean prompt = true;

    private int lineSize = 0;
    private int Y_space = 0;

    private java.util.ArrayList lines = new java.util.ArrayList();

    private int W;
    private int H;
    private int usedLines = 0;
    private int availableLines = 0;
    private Font font;

    public LinePrinter(Font fnt, boolean prompt) {
        this.setFont(fnt);
        this.prompt = prompt;

        /* Setup the paper */
        p = new Paper();
        p.setSize(W = 612, H = 792);  //8.5" x 11"
        p.setImageableArea(36, 36, W - 36, H - 36);  //half inch margins
        pgFormat.setPaper(p);

        reconfigurePage();
    }


    private void reconfigurePage() {
        /* calc linesAvailable and other metrics */
        /* get BufferedImage to create a grphics so we can do font calcs before turning the printable loose */
        BufferedImage bimage = new BufferedImage(612 - 72, 792 - 72, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bimage.createGraphics();
        g2d.setFont(font);

        FontRenderContext fr = g2d.getFontRenderContext();
        g2d = null;

        LineMetrics lm = font.getLineMetrics("HijK", fr);

        lineSize = (int) lm.getHeight();
        System.out.println("Line size: " + lineSize);

        Y_space = (int) p.getImageableHeight();
        System.out.println("Available Y_Space: " + Y_space);

        availableLines = (Y_space - lineSize) / lineSize;  //the PROPER formula for calculating available lines on a page using the specified font

        System.out.println("Available lines calc: " + availableLines);
    }

    /**
     * Adds the text Strings to the page.
     * If a line doesn't fit the page a new page is created and "printing" continues on the next page.
     */
    public void addLines(String[] text) {
        for (int i = 0; i < text.length; i++) {
            if (usedLines + 1 > availableLines) formFeed();

            lines.add(text[i]);
            usedLines++;
        }
    }


    /**
     * Adds a line of text to the page.
     * If a line goes over the page a new page is created and "printing" continues on the next page.
     * You can create more methods to accept Vectors/Genrics and what not.
     * Real new lines inside the string are ignored when rendering.
     * This can be changed by modifying this method to scan the string for the amount of newlines and then calling the newLine() method for each one.
     */
    public void addLine(String line) {
        //labels.append(p, pgFormat);
        if (usedLines + 1 > availableLines) formFeed();
        lines.add(line);
        usedLines++;
    }


    /**
     * Adds a "new line" (blank line).
     */
    public void newLine() {
        addLine("");
    }

    /**
     * Manually starts a new page.
     */
    public void formFeed() {
        book.append(new LinePrinterPage(lines, font), pgFormat);
        lines = new java.util.ArrayList();
        usedLines = 0;
    }


    /**
     * Starts a new "page" with a different font
     */
    public void formFeed(Font f) {
        book.append(new LinePrinterPage(lines, font), pgFormat);
        font = f;
        lines = new java.util.ArrayList();
        usedLines = 0;
        reconfigurePage();
    }


    /**
     * The whole document will be rendered in this font until forFeed(Font f) is called at least.
     */
    private void setFont(Font f) {
        font = f;
    }

    /**
     * Set the orientation before calling formFeed() or go().  Valid strings are "portrait" and "landscape"
     */
    public void setOrientation(String orient) {
        if (orient.equalsIgnoreCase("portrait")) {
            pgFormat.setOrientation(PageFormat.PORTRAIT);
        } else {
            if (orient.equalsIgnoreCase("landscape")) {
                pgFormat.setOrientation(PageFormat.LANDSCAPE);
            }
        }
    }

    /**
     * Call formFeed() before calling go()
     */
    public void go() {
        /* Print the Book */
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPageable(book);  //contains all pgFormats

        boolean doJob = true;
        if (prompt) {
            doJob = printJob.printDialog();
        }
        if (doJob) {
            try {
                printJob.print();
            } catch (Exception PrintException) {
                PrintException.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        //                                         Select Printer
        LinePrinter dp = new LinePrinter(new Font("roman", Font.PLAIN, 10), false);  //Regular DYMO ~ Co Star LabelWriter Labels '30252'
        dp.addLines(new String[]{"", "Data Virtue", "123 Road Street", "Hillsboro, OH  45133"});
        dp.formFeed();

        for (int i = 1; i < 74; i++) {
            dp.addLines(new String[]{"John Doe", "", "123 Road Street", "Hillsboro, OH  45133"});
            //dp.formFeed(); //put each address on a new page
        }

        dp.go();
    }

}//END LinePrinter class


/**
 * When printing the various strings for the page new lines within the string are ignored
 */
class LinePrinterPage implements Printable {  //this can access the vars in

    /**
     * Creates a new instance of LinePrinterPage
     */
    public LinePrinterPage(java.util.ArrayList lines, Font f) {
        this.lines = lines;
        font = f;
    }

    private java.util.ArrayList lines;
    private Font font;

    public int print(Graphics g, PageFormat pageFormat, int page) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(Color.black);
        g2d.setFont(font);
        g2d.setClip(null);

        FontRenderContext fr = g2d.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("HijK", fr);

        double X = pageFormat.getImageableX();
        double fh = (int) lm.getHeight();
        double Y = pageFormat.getImageableY();

        for (int i = 0; i < lines.size(); i++) {
            /* real new lines inside text strings are ignored */
            g2d.drawString((String) lines.get(i), (float) X, (float) Y);
            Y += fh;   // keeping track of how much was written
        }

        return (PAGE_EXISTS);
    }
}
