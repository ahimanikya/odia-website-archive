import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////
// Description: a simple digital clock in Java (1.0 applet)
// Author: Muhammad A Muquit
// Classification: unclassified
// email: muquit@semcor.com
// URL: http://www.semcor.com/~muqit/
//
// 12/05/1995           lets play with Java
// 01/29/96             some clean up
// 02/13/96             added ornamental frame
//                      added 24 hr time format (TimeFormat)
// 02/15/96             GMT+-hhmm to display time of any place (TZ)
//                      date (ShowDate)
// 03/13/96             using MediaTrcaker to stop incremental display
//                      Suggested by Jim Graham
// 03/16/96             display clock without frame.
//                      shows version info while mouse is moved on the appl.
//                      stop clock by clicking mouse
// Copyright:
// This program is in the public domain. Do anything you like with it.
// It would be nice if you give me credit for it tho.
//////////////////////////////////////////////////////////

/*

Example of calling convention:

The default 24 hr format:

<applet code="Dgclock.class" width=100 height=30></applet>

To display time in 12 hr format:

<applet code="Dgclock.class" width=100 height=30>
<param name="TimeFormat" value="%I">
</applet>

To display NewYork time:

<applet code="Dgclock.class" width=100 height=30>
<param name="TZ" value="GMT-0500">
</applet>

12 or 24 hr format
<param name="TimeFormat" value="%H">    (24 hr format - default)
<param name="TimeFormat" value="%I">    (12 hr format)

the format for value is GMT+-hhmm, for example

<param name="TZ" value="GMT+0600">    (6 hours west of Greenwich meridian)
<param name="TZ" value="GMT+0430">    (4 hours, 30 minutes
                                      west of Greenwich meridian)
<param name="TZ" value="GMT+0000">    (Greenwich)
<param name="TZ" value="GMT-0500">    (5 hours east of Greenwich meridian)

<param name="ShowDate" value="yes">   (show date with time)
<param name="ShowDate" value="no">   (don't show date - default)

<applet code="Dgclock.class" width=89 height=20>
<param name="ShowFrame" value="no">   (do not wrap clock with frame)

<param name="ShowVersion" value="no"> (do not show version info when the
                                       mouse cursor is moved on the applet)

*/

public class Dgclock extends Applet implements Runnable
{
    MediaTracker
        tracker;

    Thread
        timer=null;

    boolean
        suspended=false;

    Image[]
        simage=new Image[10],
        image=new Image[11];

    Image
        frame_image,
        buf_image;

    Graphics
        gc;

    int
        bdigit_width=15;

    int
        sdigit_width=7;
    int
        sdigit_height=9;

    int
        base_width=100;

    int
        base_height=30;

    int
        offset=4;

    String
        TimeFormat="%H";

    long
        TimeDiff=0L;

    boolean
        ShowDate=false;
    boolean
        ShowStatus=true;

    int checkTZ(String TZ)
    {
       int
          d;

       int
            hr,
            min;
                 
       int   
            counter=0;

       int diff=0; 
     
       hr=0;
       min=0;
       if (TZ != null)
       { 
            int length=TZ.length();
            if (length == 8)
            {
                //Print("Length= " + length);
                int c=TZ.charAt(3);
                if ((c == '-') || (c == '+'))
                {
                    String tzval=TZ.substring(4,8);
                    //Print("TZ value= " + tzval);
             
                    for (int i=0; i < tzval.length(); i++)
                    {
                        d=tzval.charAt(i);
         
                        if ( (d >= '0') && d <= '9')
                        {
                            if (counter < 2)  
                                hr=hr*10+(d-'0');
                            else  
                                min=min*10+(d-'0');
                            counter++;
                        }
                    }
                    //Print("Hour: " + hr + " Min: " + min);
                    diff=(hr*60)+min;
                    if (c == '-')
                        diff=(-diff);
                }
            }
       }

       return diff;
    }  

    public void init()
    {
        int
            x,
            y;

        // check if TimeFormat is supplied
        TimeFormat=getParameter("TimeFormat");
        if (TimeFormat != null)
        {
            if  ((!TimeFormat.equals("%I")) && (!TimeFormat.equals("%H")))
                TimeFormat="%H";
        }
        else
            TimeFormat="%H";

        String gp=getParameter("ShowDate");
        if (gp != null)
        {
            if (gp.equals("yes"))
                ShowDate=true;
        }

        gp=getParameter("ShowVersion");
        if (gp != null)
        {
            if (gp.equals("no"))
                ShowStatus=false;
        }

        // check if frame need to be displayed
        gp=getParameter("ShowFrame");
        if (gp != null)
        {
            if (gp.equals("no"))
            {
                offset=0;
                base_width=89;
                base_height=20;
            }
        }     

        //Print("TImeForamt= " + TimeFormat);

        tracker=new MediaTracker(this);
        if (offset == 4)
        {
            frame_image=getImage(getCodeBase(),"jimages/frame.gif");
            tracker.addImage(frame_image,0);
        }

        for (int i=0; i <= 10; i++)
        {
            image[i]=getImage(getCodeBase(),"jimages/h" + i + ".gif");
            tracker.addImage(image[i],0);
        }

        // load the sec images
        for (int i=0; i <= 9; i++)
        {
            simage[i]=getImage(getCodeBase(),"jimages/s" + i + ".gif");
            tracker.addImage(image[i],0);
        }

        // check for TZ
        int
            diff=0;
        String timezone=getParameter("TZ");
        if (timezone != null)
        {
            if (timezone.equals("LOCAL"))
                TimeDiff=0L;
            else
            {
                Date date=new Date();
                diff=checkTZ(timezone);
                TimeDiff=((long) date.getTimezoneOffset())*60000+diff*60000;
            }

        }

        try
        {
            buf_image=createImage(base_width,base_height);
            gc=buf_image.getGraphics();
        } catch (Exception e) gc=null;
    }
    
    public void start()
    {
        if (timer == null)
        {
            timer=new Thread(this, "Dgclock");
            timer.start();
        }
    }

    public void run()
    {
        while (timer != null)
        {
            try
            {
               timer.sleep(1000);
            }catch (InterruptedException e)
             {
                break;
             }
            repaint();
        }
        timer=null;
    }
    
    public void paintApplet(Graphics g)
    {
        int
            i,
            j,
            k;

        Date
            now=new Date();

        int
            hour,
            minute,
            second;

        String
            hr,
            min,
            sec,
            today;

        if (TimeDiff != 0L)
        {
            now.setTime(now.getTime()+TimeDiff);
        }
        hr=formatDate(TimeFormat,now);
        min=formatDate("%M",now);
        sec=formatDate("%S",now);

        if (ShowDate == true)
        {
            today=formatDate("%d",now);
        }
        else
            today="";


        // draw the frame
        if (offset == 4)
            g.drawImage(frame_image,0,0,this);
        
        // draw the hour digit images
        for (i=0; i <hr.length(); i++)
        {
            g.drawImage(image[hr.charAt(i)-'0'],(i*bdigit_width)+
                offset,offset,this);
        }

        // draw the colon image
        g.drawImage(image[10],(i*bdigit_width)+offset,offset,this);
        i++;

        // draw the minute digit images
        for (j=0; j < min.length(); j++)
        {
            g.drawImage(image[min.charAt(j)-'0'],(i*bdigit_width)+
                offset,offset,this);
            i++;
        }

        // draw the second digit images
        for (j=0; j < sec.length(); j++)
        {
            g.drawImage(simage[sec.charAt(j)-'0'],(i*bdigit_width)+
                (j*sdigit_width)+offset,offset,this);
        }

        if (ShowDate == true)
        {
            for(k=0; k < today.length(); k++)
            {
                g.drawImage(simage[today.charAt(k)-'0'],
                    (i*bdigit_width)+(k*sdigit_width)+offset,
                        (offset+sdigit_height+1), this);
            }
        }
    }

    public void update(Graphics g)
    {
        if (tracker.statusID(0,true) == MediaTracker.COMPLETE)
        {
            if (buf_image != null)
            {
                if (offset == 0)
                {
                    gc.setColor(Color.black);
                    gc.fillRect(0,0,base_width,base_height);
                }
                paintApplet(gc);
                g.drawImage(buf_image,0,0,this);
            }
            else
            {
                g.clearRect(0,0,base_width,base_height);
                paintApplet(g);
            }
        }
    }
// Left Pad a number
// taken from Clock2 by Per Reedtz Thomsen/Rachel Gollub
private String padElement(int expr, char padChar)
{
  String result = "";
  // I'm just padding 2 digit numbers
  if (expr < 10) result = result.concat(String.valueOf(padChar));
  result = result.concat(String.valueOf(expr));
  return(result);  
}

// Format a date according to the formatting string.
// taken from Clock2 by Per Reedtz Thomsen/Rachel Gollub
private String formatDate(String fmt, Date d)
{
  String formattedDate = "";
      
  // Retrieve the specific date information
  int hour = d.getHours();
  int minute = d.getMinutes();
  int second = d.getSeconds();
  int monthDay = d.getDate();
        
  int US_Hour = hour < 13 ? hour : hour - 12;
      
  // Loop through the format string
  for(int i = 0; i < fmt.length(); i++)
  {
    if (fmt.charAt(i) == '%') // We've hit a formatting command...
    {
      i++; // Move past the '%' sign
      // Figure out the format.
      switch (fmt.charAt(i))
      {
      case 'H': // Hour -- 00 to 23
        formattedDate = formattedDate.concat(padElement(hour, '0'));
        break;
      case 'I': // Hour -- 01 to 12
        formattedDate = formattedDate.concat(padElement(US_Hour, '0'));
        //formattedDate=formattedDate.concat(String.valueOf(US_Hour));
        break;
      case 'M': // Minutes -- 00 to 59
        formattedDate = formattedDate.concat(padElement(minute, '0'));
        break;
      case 'd': // 2 digit month number
        formattedDate = formattedDate.concat(padElement(monthDay, '0'));
        break;
      case 'S': // Second -- 00 to 61 (leap seconds)
        formattedDate = formattedDate.concat(padElement(second, '0'));
        break;
      default:
        formattedDate = formattedDate.concat("??");
        break;
      }
    }
    else // A regular character
    {
      formattedDate = formattedDate.concat(String.valueOf(fmt.charAt(i)));
    }
  } // end for

  return(formattedDate);
}

    public void stop()
    {
        timer.stop();
        timer=null;
    }

    public boolean handleEvent(Event evt)
    {
        switch(evt.id)
        {
            case Event.MOUSE_DOWN:
            {
                if (suspended)
                {
                    timer.resume();
                }
                else
                {
                    //Print ("Suspending..");
                    timer.suspend();
                }
                suspended = !suspended;
                break;
            }
            
            case Event.MOUSE_MOVE:
            {
                if (tracker.statusID(0,true) != MediaTracker.COMPLETE)
                {
                    showStatus("Loading images...please wait!");
                }
                else
                {
                    if (suspended == false)
                    {
                    if (ShowStatus == true)
                    showStatus("Dgclock 2.1 by muquit@semcor.com");
                    }
                }
                break;
            }
        }
        return true;
    }

    /*
    public void Print(String str)
    {
        System.out.println(str);
    }
    */

}
