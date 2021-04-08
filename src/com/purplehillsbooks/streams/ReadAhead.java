package com.purplehillsbooks.streams;

import java.io.Reader;


/**
 * This is a simple class.  It managed a reader.
 * I reads one character ahead, and allows you
 * to test this "next" character many times.
 * Consume that character by reading the next, which
 * can be tested many times.
 * This allows you to parse text recursively and never
 * read a charater more than once, even though it is
 * needed to be tested at many levels.
 *
 * It also tracks line number and column number in case you
 * want to report where you find a problem
 */
public class ReadAhead {

    public int ch;
    private Reader r;
    private int lineNo;
    private int colNo;

    public ReadAhead(Reader reader) throws Exception {
        r = reader;
        lineNo = 1;
        colNo = 1;
        ch = r.read();
    }

    public int nextChar() {
        return ch;
    }
    public int lineNo() {
        return lineNo;
    }
    public int colNo() {
        return colNo;
    }

    public int read() throws Exception {
        if (ch=='\n') {
            lineNo++;
            colNo = 1;
        }
        else {
            colNo++;
        }
        ch = r.read();
        while (ch=='\r') {
            //consume line feed character these so they are never seen
            ch = r.read();
        }
        return ch;
    }

    public void skipToNewLine() throws Exception {
        while (ch!='\n') {
            read();
            if (ch<0) {
                //end of file, stop reading
                return;
            }
        }
        //now read past the end of line character to start new line
        read();
    }

}
