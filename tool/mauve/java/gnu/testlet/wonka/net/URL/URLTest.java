// Tags: JDK1.0

/*
   Copyright (C) 1999 Hewlett-Packard Company

   This file is part of Mauve.

   Mauve is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   Mauve is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Mauve; see the file COPYING.  If not, write to
   the Free Software Foundation, 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/

package gnu.testlet.wonka.net.URL;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.net.*;
import java.io.IOException; 


public class URLTest implements Testlet
{
  protected static TestHarness harness;
	public void test_Basics()
	{
		// see whether malformed exception is thrown or not.

		harness.checkPoint("Constructors");
		try {
			URL url = new URL("hithleksjf" );
			harness.fail("Error in test_Basics  - 1 " + 
				" should have raised malformed URL exception here");

		}
		catch ( MalformedURLException e ){
			harness.check(true);
		}

		try {
			URL url = new URL("http://////://" );
			harness.check(true);
		}
		catch ( MalformedURLException e ){
			harness.fail("Error in test_Basics  - 2 " + 
				" should not have raised malformed URL exception here");
		}


		try {
;			URL url = new URL("http://sources.redhat.com/index.html" );
			harness.check(true);
		}
		catch ( MalformedURLException e ){
			harness.fail("Error in test_Basics  - 3 " + 
				" should not have raised malformed URL exception here");
		}

		// URL with individual arguments.
		harness.checkPoint("get Methods");
		try {
			URL baseurl = new URL("http://sources.redhat.com/");
			URL url = new URL ( baseurl, "index.html");
			url.hashCode();
			baseurl.hashCode();
			URL.setURLStreamHandlerFactory( null );
			harness.check (url.getProtocol(), "http");
			harness.check (url.getPort(), -1);
			harness.check (url.getHost(), "sources.redhat.com");
                        // CG 20060703 - this is how I read RFC 2396 ...
			harness.check (url.getAuthority(), "sources.redhat.com");
			harness.check (url.getFile(), "/index.html");
			harness.check (url.equals(new URL("http://sources.redhat.com/index.html")));
                        // CG 20070702 - it can be 0 if it wants to be
			// harness.check (url.hashCode() != 0);

			url = new URL("http://username:password@sources.redhat.com");
			url.hashCode();
			harness.check (url.getHost(), "sources.redhat.com");
                        // CG 20060703 - this is how I read RFC 2396 ...
			harness.check (url.getAuthority(), "username:password@sources.redhat.com");
			harness.check (url.getFile(), "");
                        // test our workaround for a common bogosity
			url = new URL ( baseurl, "http:index.html");
			harness.check (url.getProtocol(), "http");
			harness.check (url.getPort(), -1);
			harness.check (url.getHost(), "sources.redhat.com");
			harness.check (url.getAuthority(), "sources.redhat.com");
			harness.check (url.getFile(), "/index.html");
		}
		catch ( MalformedURLException e ){
				harness.fail(" Error in test_Basics  - 9 " + 
					" exception should not be thrown here");
		}


		try {
			URL url = new URL ( "http", "sources.redhat.com", "/index.html");

			harness.check (url.getProtocol(), "http");
			// CG 20051030 - see above
			// harness.check (url.getPort(), -1);
			harness.check (url.getHost(), "sources.redhat.com");
			harness.check (url.getFile(), "/index.html");
			harness.check (url.equals(new URL("http://sources.redhat.com/index.html")));

			URL url1 = new URL ( "http", "sources.redhat.com", 80,  "index.html");
			harness.check (url1.getPort(), 80);
		}
		catch ( MalformedURLException e ){
				harness.fail(" Error in test_Basics  - 16 " + 
					" exception should not be thrown here");
		}


		try {
			URL url = new URL ( "http://sources.redhat.com:80/mauve/testarea/index.html");

			harness.check (url.getProtocol(), "http");
			harness.check (url.getPort(), 80);
			harness.check (url.getHost(), "sources.redhat.com");
			harness.check (url.getFile(), "/mauve/testarea/index.html");
		}
		catch ( MalformedURLException e ){
				harness.fail(" Error in test_Basics  - 21 " + 
					" exception should not be thrown here");
		}
	}

	/* CG - none of this stuff works any more ...
	public void test_openConnection()
	{
		harness.checkPoint("openConnection");
		try {
			URL url = new URL ( "http://sources.redhat.com/mauve/testarea/index.html");

			URLConnection conn = url.openConnection();

			harness.check (conn.getHeaderField(2), "Apache/1.3.9 (Unix)");
			String conttype	= conn.getContentType();
			harness.check (conttype, "text/html");

			Object obj = url.getContent();
			harness.check (url.toExternalForm(),
				"http://sources.redhat.com/mauve/testarea/index.html");
			harness.check (url.getRef(), null);

			URL url2 = new URL("http://www.hhp.com/index.html#help");
			harness.check (url2.getRef(), "help");
		}catch ( Exception e ){
				harness.fail(" Error in test_openConnection  - 3 " + 
					" exception should not be thrown here");
				e.printStackTrace(System.out);
		}		

	}
        */


	public void test_openStream()
	{
		harness.checkPoint("openStream");
		try {
			byte[] b = new byte[6];
			URL url = new URL("http://sources.redhat.com/mauve/testarea/index.html");
			java.io.InputStream conn = url.openStream();
			conn.read(b , 0 , 6 );

			String str = new String( b ) ;
			harness.check (str, "<HTML>");

		}catch ( Exception e ){
			harness.fail(" Error in test_openStream  - 2 " + 
					e + " should not be thrown here");
		}		
	}


	public void test_sameFile()
	{
		harness.checkPoint("sameFile");
		try {
			URL url = new URL ( "http://sources.redhat.com/mauve/testarea/index.html");
			URL url1 = new URL ( "http://sources.redhat.com/mauve/testarea/index.html");
			harness.check (url.sameFile(url1));

			URL url2 = new URL ( "http://sources.redhat.com:80/mauve/testarea/index.html");
			harness.check (url.sameFile(url2));

		}catch ( Exception e ){
			harness.fail(" Error in test_sameFile  - 3 " + 
					" exception should not be thrown here");
		}

	}
	

	/* CG - if you ask me you can't rely on any of this stuff
	public void test_toString()
	{
		harness.checkPoint("toString");
		try {
			URL url = new URL ( "http://sources.redhat.com/index.html");
			String str = url.toString();

			URL url1 = new URL ( "http://sources.redhat.com:80/mauve/testarea/index.html");
			String str1 = url1.toString();

			URL url2 = new URL ( "http://205.180.83.71/");
			String str2 = url2.toString();

			harness.check (str, "http://sources.redhat.com/index.html");
			harness.check (str1, "http://sources.redhat.com:80/mauve/testarea/index.html");
			harness.check (str2, "http://205.180.83.71/");

			URL url3 = new URL( "ftp" , "sources.redhat.com" , 21 , "/dir/dir1.lst");
			String str3 = url3.toString( );

			harness.check (str3, "ftp://sources.redhat.com:21/dir/dir1.lst");
		}catch ( Exception e ){
			harness.fail(" Error in test_toString  - 5 " + 
					" exception should not be thrown here");
		}		
	}
	*/

        /* [CG 20110610]
         * None of this is allowed because sh has to be the URLStreamHandler
         * which originally created the URL else SecurityError.
         * <p>MyURLStreamHandler was just a thin wrapper around URLStreamHandler
         * so I don't know what these tests were supposed to show anyway ..
	public void test_URLStreamHandler()
	{
		harness.checkPoint("URLStreamHandler");
		try {
		  URL url = new URL ( "http://sources.redhat.com/index.html");
		// test URLStreamHandler
 		MyURLStreamHandler sh = new MyURLStreamHandler();
 		sh.invoke_setURL(url, "http", "sources.redhat.com", 80, "/index.html", "#ref");
		harness.check(true);
 		sh.invoke_parseURL(url, "http://sources.redhat.com/index.html", 0, 20);
		harness.check(true);
		}catch ( MalformedURLException e ){
			harness.fail(" Error in test_URLStreamHandler  - 1 " + 
					" exception should not be thrown here");
		}
	}
      */

        public void test_cr601a() {
            String[][] s = {

                // tests 1..2
                {"file:///c:/pub/files/foobar.txt",
                 "file:/c:/pub/files/foobar.txt",
                 "",
                 "/c:/pub/files/foobar.txt"},

                // tests 3..4
                {"file://hpjavaux/c:/pub/files/foobar.txt",
                 "file://hpjavaux/c:/pub/files/foobar.txt",
                 "hpjavaux",
                 "/c:/pub/files/foobar.txt"},

                // tests 5..6
                {"file://c:/pub/files/foobar.txt",
                 "file://c/pub/files/foobar.txt",
                 "c",
                 "/pub/files/foobar.txt"},

                // tests 7..8
                {"file:/c:/pub/files/foobar.txt",
                 "file:/c:/pub/files/foobar.txt",
                 "",
                 "/c:/pub/files/foobar.txt"},

                // tests 11..12
                {"file:///hpjavant/bgee/foobar.txt",
                 "file:/hpjavant/bgee/foobar.txt",
                 "",
                 "/hpjavant/bgee/foobar.txt"},

                // tests 13..14
                {"file://hpjavant/bgee/foobar.txt",
                 "file://hpjavant/bgee/foobar.txt",
                 "hpjavant",
                 "/bgee/foobar.txt"},

                // tests 15..16
                {"file:/hpjavant/bgee/foobar.txt",
                 "file:/hpjavant/bgee/foobar.txt",
                 "",
                 "/hpjavant/bgee/foobar.txt"},

                // tests 17..18
                {"file://hpjavaux//hpjavant/bgee/foobar.txt",
                 "file://hpjavaux//hpjavant/bgee/foobar.txt",
                 "hpjavaux",
                 "//hpjavant/bgee/foobar.txt"},

                // tests 19..20
                {"file://hpjavaux/bgee/foobar.txt",
                 "file://hpjavaux/bgee/foobar.txt",
                 "hpjavaux",
                 "/bgee/foobar.txt"},

                // tests 21..22
                {"file://hpjavaux/c:/pubs/files/foobar.txt",
                 "file://hpjavaux/c:/pubs/files/foobar.txt",
                 "hpjavaux",
                 "/c:/pubs/files/foobar.txt"},

                // tests 23..24
                {"file://bg710571//hpjavant/bgee/foobar.txt",
                 "file://bg710571//hpjavant/bgee/foobar.txt",
                 "bg710571",
                 "//hpjavant/bgee/foobar.txt"},

                // tests 25..27
                {"file://bg710571/bgee/foobar.txt",
                 "file://bg710571/bgee/foobar.txt",
                 "bg710571",
                 "/bgee/foobar.txt"},

                // tests 28..29
                {"file://bg710571/c:/pubs/files/foobar.txt",
                 "file://bg710571/c:/pubs/files/foobar.txt",
                 "bg710571",
                 "/c:/pubs/files/foobar.txt"},
            };

            harness.checkPoint("new URL(string)");
            for (int i = 0; i < s.length; ++i) {
               try {
                    URL url = new URL(s[i][0]);
                    //harness.check(url.toExternalForm(), s[i][1]);
                    harness.check(url.getHost(), s[i][2]);
                    harness.check(url.getFile(), s[i][3]);
                }
                catch (Throwable e) {
                    harness.fail("Should not have thrown exception");
		    e.printStackTrace(System.out);
                }
            }
        }

        public void test_cr601b() {
            String[][] s = {

                // tests 1..3
                {"////", "c:/pub/files/foobar.txt",
                 "file://////c:/pub/files/foobar.txt",
                 "////",
                 "c:/pub/files/foobar.txt"},

                 // tests 4..6
                {"///", "c:/pub/files/foobar.txt",
                 "file://///c:/pub/files/foobar.txt",
                 "///",
                 "c:/pub/files/foobar.txt"},

                 // tests 7..9
                {"//", "c:/pub/files/foobar.txt",
                 "file:////c:/pub/files/foobar.txt",
                 "//",
                 "c:/pub/files/foobar.txt"},

                 // tests 10..12
                {"/", "c:/pub/files/foobar.txt",
                 "file:///c:/pub/files/foobar.txt",
                 "/",
                 "c:/pub/files/foobar.txt"},

                 // tests 13..15
                {"", "c:/pub/files/foobar.txt",
                 "file:c:/pub/files/foobar.txt",
                 "",
                 "c:/pub/files/foobar.txt"},

                 // tests 16..18
                {"hpjavaux", "c:/pub/files/foobar.txt",
                 "file://hpjavauxc:/pub/files/foobar.txt",
                 "hpjavaux",
                 "c:/pub/files/foobar.txt"},

                 // tests 19..21
                {"////", "//hpjavant/bgee/foobar.txt",
                 "file:////////hpjavant/bgee/foobar.txt",
                 "////",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 22..24
                {"///", "//hpjavant/bgee/foobar.txt",
                 "file:///////hpjavant/bgee/foobar.txt",
                 "///",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 25..27
                {"//", "//hpjavant/bgee/foobar.txt",
                 "file://////hpjavant/bgee/foobar.txt",
                 "//",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 28..30
                {"/", "//hpjavant/bgee/foobar.txt",
                 "file://///hpjavant/bgee/foobar.txt",
                 "/",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 31..33
                {"", "//hpjavant/bgee/foobar.txt",
                 "file://hpjavant/bgee/foobar.txt",
                 "",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 34..36
                {"hpjavaux", "//hpjavant/bgee/foobar.txt",
                 "file://hpjavaux//hpjavant/bgee/foobar.txt",
                 "hpjavaux",
                 "//hpjavant/bgee/foobar.txt"},

                 // tests 37..39
                {"hpjavant", "/bgee/foobar.txt",
                 "file://hpjavant/bgee/foobar.txt",
                 "hpjavant",
                 "/bgee/foobar.txt"},

                 // tests 40..42
                {"hpjavant", "/home/bgee/foobar.txt",
                 "file://hpjavant/home/bgee/foobar.txt",
                 "hpjavant",
                 "/home/bgee/foobar.txt"},

                 // tests 43..45
                {"hpjavaux", "/home/bgee/foobar.txt",
                 "file://hpjavaux/home/bgee/foobar.txt",
                 "hpjavaux",
                 "/home/bgee/foobar.txt"},
            };
            harness.checkPoint("new URL(protocol, host, file)");
            for (int i = 0; i < s.length; ++i) {
               try {
                    URL url = new URL("file", s[i][0], s[i][1]);
                    //harness.check(url.toExternalForm(), s[i][2]);
                    harness.check(url.getHost(), s[i][3]);
                    harness.check(url.getFile(), s[i][4]);
                    harness.check(true);
                }
                catch (NullPointerException e) {
                    if ((i != 6) && (i != 13)) {
			harness.fail("Should not have thrown NullPointerException");
			e.printStackTrace(System.out);
                    }
                }
                catch (Throwable e) {
		    harness.fail("Should not have thrown exception");
		    e.printStackTrace(System.out);
                }
            }
        }

	public void testall()
	{
		test_Basics();
		//test_openConnection();
		test_openStream();
		test_sameFile();
		//test_toString();
		//test_URLStreamHandler();
                test_cr601a();
                test_cr601b();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    harness.setclass("java.net.URL");
    testall ();
  }

}
