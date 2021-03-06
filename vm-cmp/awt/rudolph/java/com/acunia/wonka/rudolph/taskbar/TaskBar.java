/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph.taskbar;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class TaskBar extends Frame implements FocusListener {

  public static final int BOTTOM = 0;
  public static final int TOP = 1;
  public static final int LEFT = 2;
  public static final int RIGHT = 3;
  public static final int FLOATING = 4;

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  
  private static boolean enabled = false;
  private static int location = TOP;
  private static int size = 0;
  private static Properties properties;
  private static Rectangle floatDimensions;

  private static Image background;
  private static Color color;

  private static TaskBar taskbar = null;

  private int tb_width;
  private int tb_height;

  private Vector applets;

  public static TaskBar getTaskBar() {
    if(properties == null) loadProperties();
    if(taskbar == null && enabled) taskbar = new TaskBar();
    return taskbar;
  }

  public TaskBar() {
    setLayout(new TaskBarLayout());

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int tx;
    int ty;

    if(location == FLOATING) {
      tb_width = floatDimensions.width;
      tb_height = floatDimensions.height;
      tx = floatDimensions.x;
      ty = floatDimensions.y;
    }
    else {  
      tb_width = (location == LEFT || location == RIGHT ? size : screen.width);
      tb_height = (location == TOP || location == BOTTOM ? size : screen.height);
      tx = (location == RIGHT ? screen.width - size : 0);
      ty = (location == BOTTOM ? screen.height - size : 0);
    }

    setBounds(tx, ty, tb_width, tb_height);
    setBackground(Color.black);

    addFocusListener(this);

    applets = new Vector();

    loadApplets();

    setVisible(true);
  }

  /*
  ** Load the taskbar properties.
  */

  private static void loadProperties() {
    properties = new Properties();
  
    try {
      properties.load(ClassLoader.getSystemResourceAsStream("taskbar.properties"));
    } 
    catch(Exception e) {
    }

    enabled = (Boolean.valueOf(properties.getProperty("taskbar", "false"))).booleanValue();

    if(!enabled) {
      
      return;
      
    }

    String loc = properties.getProperty("taskbar.location", "top");
    if(loc.equals("top")) {
      location = TOP;
    }
    else if(loc.equals("bottom")) {
      location = BOTTOM;
    }
    else if(loc.equals("left")) {
      location = LEFT;
    }
    else if(loc.equals("right")) {
      location = RIGHT;
    }
    else if(loc.equals("floating")) {
      location = FLOATING;
      floatDimensions = new Rectangle(Integer.parseInt(properties.getProperty("taskbar.x", "0")), 
                                      Integer.parseInt(properties.getProperty("taskbar.y", "0")), 
                                      Integer.parseInt(properties.getProperty("taskbar.width", "0")), 
                                      Integer.parseInt(properties.getProperty("taskbar.height", "0")));
    }

    size = (loc.equals("floating") ? floatDimensions.width : Integer.parseInt(properties.getProperty("taskbar.size", "32")));

    String imgname = properties.getProperty("taskbar.background", null);
    if(imgname != null) {
      URL url = ClassLoader.getSystemResource(imgname);
      if(url != null) {
        background = Toolkit.getDefaultToolkit().getImage(url);
      }
    }

    int r = Integer.parseInt(properties.getProperty("taskbar.color.red", "0"));
    int g = Integer.parseInt(properties.getProperty("taskbar.color.green", "0"));
    int b = Integer.parseInt(properties.getProperty("taskbar.color.blue", "0"));
    color = new Color(r, g, b);
  }

  /*
  ** Load the applets.
  */

  private void loadApplets() {
    String classname = null;
    int i = 1;
    TaskBarConstraints constr;
    TaskBarApplet applet;
    Class appletClass;
    Constructor appletConstructor;

    while((classname = properties.getProperty("taskbar.applet." + i + ".class")) != null) {
      int position = Integer.parseInt(properties.getProperty("taskbar.applet." + i + ".position", "-1"));
     
      try {
        appletClass = Class.forName(classname);
        appletConstructor = appletClass.getConstructor(new Class[]{TaskBar.class});
        applet = (TaskBarApplet)appletConstructor.newInstance(new Object[]{this});
      
        if(position != -1) {
          constr = new TaskBarConstraints(position);
        }
        else {
          constr = new TaskBarConstraints();
        }

        addApplet(applet, constr);
      }
      catch(Exception e) {
        System.out.println("Oh no : " + e);
      }
      
      i++;
    }
    
  }

  /*
  ** Get the taskbar size, location & orientation.
  */
  
  public static int getBarSize() {
    return size;
  }

  public static int getBarLocation() {
    return location;
  }

  public static int getBarOrientation() {
    return (location == LEFT || location == RIGHT ? VERTICAL : HORIZONTAL);
  }

  /*
  ** Size can not be changed, that's why these 3 return the same size.
  */

  public Dimension getPreferredSize() {
    return new Dimension(tb_width, tb_height);
  }
  
  public Dimension getMinimumSize() {
    return new Dimension(tb_width, tb_height);
  }
  
  public Dimension getMaximumSize() {
    return new Dimension(tb_width, tb_height);
  }

  /*
  ** Applet stuff.
  */

  public void addApplet(TaskBarApplet applet, TaskBarConstraints consts) {
    add(applet, consts);
    applets.add(applet);
  }

  public int getAppletCount() {
    return applets.size();
  }

  public TaskBarApplet getApplet(int i) {
    return (TaskBarApplet)applets.elementAt(i);
  }
  
  /*
  ** paint -> draws the background image.
  */
  
  public void paint(Graphics g) {
    g.setColor(color);
    g.fillRect(0, 0, getSize().width, getSize().height);
    if(background != null) {
      g.drawImage(background, 0, 0, null);
    }
  }

  /*
  ** Get rid of the focus if we receive it.
  */
  
  public void focusGained(FocusEvent evt) {
    Component.revertFocus();
  }

  public void focusLost(FocusEvent evt) {
  }

  /*
  ** Properties.
  */

  public Properties getProperties() {
    return properties;
  }

}

