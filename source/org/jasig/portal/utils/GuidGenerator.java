/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.net.InetAddress;
import java.util.Random;

/**
  * Creates a Global/Universal Unique ID, per DCE RPC specification.
  * Requires seed of MAC address/node identifier (12-digit hex string) for uniqueness.
  * Specification found at the OpenGroup.Org web site.
  * @author Michael Ivanov
  * @version $Revision$
  */
public class GuidGenerator {
    /**
     * CLOCKMOD constitutes the net value range for the clock counter per RFC.
     *
     * @see GuidGenerator#setClockSeq
     */
    private final static short CLOCKMOD = 16384;

    /**
     * NANOS is the conversion from millisecond timer to 100 nanoseconds per RFC.
     *
     * @see GuidGenerator#getMilliTime
     */
    private final static short NANOS = 10000;

    /**
     * nanoCounter holds static iteration value for unique nanosecond value when
     * two Guid values requested within clock resolution (one millisecond).
     *
     * @see GuidGenerator#getNano
     *
     * @see GuidGenerator#nanoBump
     *
     * @see GuidGenerator#nanoReset
     *
     * @see GuidGenerator#set
     */
    private static long nanoCounter = 0;

    /**
     * clockSeq holds the static clock sequence string used in fourth element of Guid per RFC.
     *
     * @see GuidGenerator#setClockSeq
     */
    private static String clockSeq;

    /**
     * lastMilliTime holds the static last millisecond time requested in order to provide determination
     * if nanoCounter needs to be adjusted.
     *
     * @see GuidGenerator#getLastTime
     *
     * @see GuidGenerator#setLastTime
     *
     * @see GuidGenerator#set
     */
    private static long lastMilliTime;

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //
    // class instance vars
    //
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    /**
     * macbase holds the instance initialization string denoting fifth element of Guid per RFC
     *
     * @see GuidGenerator#GuidGenerator(String)
     *
     * @see GuidGenerator#toString
     */
    private String macbase;

    /**
     * timebase holds the instance string denoting elements one thru four of Guid per RFC
     *
     * @see GuidGenerator#GuidGenerator(String)
     *
     * @see GuidGenerator#set()
     */
    private String timebase;

    // Random generator
    private static Random random = new Random();

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //
    // methods
    //
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    /**
     * GuidGenerator() - default constructor
     */
    public GuidGenerator() throws Exception {

        InetAddress ip = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
        byte[] bytes = ip.getAddress();

        StringBuffer buffer = new StringBuffer(12);
        buffer.append(new String(bytes));
        int actualSize = buffer.toString().length();
        if ( actualSize < 12 ) {
            byte[] tailBytes = new byte[12-actualSize];
            random.nextBytes(tailBytes);
            sun.io.ByteToCharConverter converter = sun.io.ByteToCharConverter.getConverter ( "ASCII" );
            char[] tailChars = converter.convertAll(tailBytes);
            buffer.append(tailChars);
        }
        initGuid ( buffer.toString() );
    }

    /**
     * GuidGenerator(String) - seeded constructor
     *
     * @param newMAC as the seed value for fifth element of string GUID
     *
     * @exception IllegalArgumentException
     */
    public GuidGenerator(String newMAC) throws IllegalArgumentException {
        initGuid(newMAC);
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //
    // private routines to protect class static vars
    //
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    /**
     * getNano atomically increments and returns nanoCounter
     *
     */
    private void initGuid(String newMAC) throws IllegalArgumentException {
        // validate newMAC length
      if (newMAC.length() != 12 && newMAC.trim().length() != 12) {
          throw new IllegalArgumentException ("Guid(String) requires 12-digit string per RFC");
      }

      // set statics
      setLastTime(getMilliTime());
      setClockSeq();
      // set instance
      macbase = newMAC;
      // timebase
      set();
    }

    synchronized static long getNano() {
        nanoBump();
        return nanoCounter;
    }

    /**
     * nanoBump increments nanoCounter
     */
    synchronized static void nanoBump() { nanoCounter++; }

    /**
     * nanoReset zeroes nanoCounter
     */
    synchronized static void nanoReset() { nanoCounter = 0; }

    /**
     * getLastTime accesses lastMilliTime for return
     *
     * @return the current value of lastMilliTime
     */
    synchronized static long getLastTime () { return lastMilliTime; }

    /**
     * setLastTime sets the new value of lastMilliTime
     *
     * @see GuidGenerator#set
     *
     * @param lastTime the value to which to set lastMilliTime
     */
    synchronized static void setLastTime (long lastTime) { lastMilliTime = lastTime; }

    /**
     * getMilliTime accesses the system clock
     *
     * @return the last system time converted to 100 nanosecond intervals
     */
    synchronized static long getMilliTime () { return System.currentTimeMillis()*NANOS; }

    /**
     * getClockSeq returns the RFC clock sequence string
     *
     * @return clockSeq value
     */
    synchronized static String getClockSeq() { return clockSeq; }

    /**
     * setClockSeq sets the RFC clock sequence string
     *
     * @see GuidGenerator#GuidGenerator(String)
     */
    synchronized static void setClockSeq () {
        int clockCounter;
        final int x00FF = (new Integer(0x000000FF).shortValue());
        final int x3F00 = (new Integer(0x00003F00).shortValue());
        final int x0080 = (new Integer(0x00000080).shortValue());
        int tlen, sbfr;
        byte clock_seq_hi_res, clock_seq_low;
        String tempclockseq = "";
        //
        // create clock sequence
        //
        clockCounter = (new Double(Math.random()*CLOCKMOD).shortValue());
        // -- mask out low-order 8 bits into clock_seq_low
        sbfr = (clockCounter & x00FF);
        clock_seq_low = (new Short(new Integer(sbfr).shortValue()).byteValue());
        // -- mask out 6 least-significant high-order bits
        // -- then shift right ten places for storage into low order clock_seq_hi_res
        sbfr = (clockCounter & x3F00) >> 10;
        clock_seq_hi_res = (new Short(new Integer(sbfr).shortValue()).byteValue());
        // -- mask in 0x2 as two hi-order bits of clock_seq_hi_res
        clock_seq_hi_res |= (new Short(new Integer(x0080).shortValue()).byteValue());
        // -- reconstruct clock_seq_* into short by left-shifting clock_hi by eight
        sbfr = clock_seq_hi_res;
        sbfr = sbfr << 8;
        // -- and then OR clock_low
        sbfr |= clock_seq_low;
        // -- sbfr contains combined clock_seq_hi_res and clock_seq_low;
        // -- sbfr convers to Hex only as four right-most characters for 16-bit
        tempclockseq = Integer.toHexString(sbfr);
        tlen = tempclockseq.length();
        clockSeq = tempclockseq.substring(Math.max(tlen-4, 0),tlen);
    }

    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    //
    // public access and process methods
    //
    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
    /**
     * getNewGuid calls set to generate a new GUID
     *
     * @return the latest GUID
     */
    public String getNewGuid() {
        set();
        return toString();
    }

    /**
     * toString returns the current values as a single string per RFC
     *
     * @return the timebase from set() concatenated to the initialization string
     */
    public String toString() {
        return timebase+"-"+macbase;
    }

    /**
     * set is where all the work is done
     */
    public void set() {
        long millitime, lastmilli, nanomod = 0;
        String temptimemid = "";
        String temptimehi = "";
        int time_low, time_hi, ibfr, tlen;
        short time_mid, time_hi_ver;
        long lbfr;
        // get approximation of current 100-nanosecond offset
        millitime = getMilliTime();
        lastmilli = getLastTime();
        // adjust millitime if retrieved within millisecond
        if (millitime == lastmilli) {
            nanomod = getNano();
            millitime += nanomod;
        } else {
            nanoReset();
        }
        //
        // convert millitime to string according to time-low, time-mid, time-hi specs:
        //
        // -- mask out low-order 32 bits into time_low
        lbfr = (millitime & 0x00000000FFFFFFFFL);
        time_low = (new Long(lbfr).intValue());
        // -- mask out hi-order 32 bits into time_hi
        lbfr = (millitime & 0xFFFFFFFF00000000L)>>>32;
        time_hi = (new Long(lbfr).intValue());
        // -- mask out original 32-47 bits (0-15 now) to time_mid
        ibfr = (time_hi & 0x0000FFFF);
        time_mid = (new Integer(ibfr).shortValue());
        // -- mask out original 48-59 bits (16-27 now) to time_hi_ver bits 0-12
        ibfr = (time_hi & 0x0FFF0000)>>>16;
        time_hi_ver = (new Integer(ibfr).shortValue());
        // -- mask in version (0x4) in bits 12-15 to time_hi_ver
        time_hi_ver |= 0x4000;
        //
        // construct string per RFC:
        // <time_low>-<time_mid>-<time_hi_ver>-<clock_seq_hi_res><clock_seq_low>-<macbase>
        //
        // -- time_low converts to Hex cleanly
        // -- time_mid converts to Hex only as four right-most characters for 16-bit
        temptimemid = Integer.toHexString(time_mid);
        tlen = temptimemid.length();
        temptimemid = temptimemid.substring(Math.max(tlen-4, 0), tlen);
        // -- time_hi_ver converts to Hex only as four right-most characters for 16-bit
        temptimehi = Integer.toHexString(time_hi_ver);
        tlen = temptimehi.length();
        temptimehi = temptimehi.substring(Math.max(tlen-4, 0), tlen);
        // -- construct entire string
        timebase = Integer.toHexString(time_low) + "-" +
        temptimemid + "-" +
        temptimehi + "-" +
        getClockSeq();
        // reset lastMilliTime to be original millitime upon entry to this function
        setLastTime(millitime-nanomod);
    }

    /**
     * main is the unit testing interface that creates a new Guid instance and prints result of getNewGuid to System.out
     *
     * @param args array for input arguments
     */
    public static void main (String args[]) {
        try {
            GuidGenerator g = new GuidGenerator ("abcdefghijkl");
            System.out.println( "New GUID is " + g.getNewGuid());
            g = new GuidGenerator ();
            System.out.println( "New GUID is " + g.getNewGuid());
        } catch (Exception e) {
            System.out.println("GuidGenerator::main threw " + e.getClass() + " with message: " + e.getMessage());
        }
    }
}