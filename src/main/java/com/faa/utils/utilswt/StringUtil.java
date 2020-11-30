package com.faa.utils.utilswt;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * StringUtil.java是字符串处理工具类,它里面包括常用的进制之间的互相转换和对不同形式的电话号码进行相关的处理操作。
 */
public class StringUtil {
	public static final int FRONT = 0;

	public static final int BACK = 1;
    public static final char[] LOGIC_CHARS = new char[] { '!','^','|', '&' };
    public static final char[] BRACE_CHARS = new char[] { '{', '}','(',')', ']', '[' };
    public static final char[] COMPARE_CHARS = new char[] { '=', '<','>' };
    public static final char[] MATH_CHARS = new char[] { '+', '-','*','/', '%' };
    public static final char[] SEPARATOR_CHARS = new char[] { ',', ';',':','.','\\','/' };
    public static final char[] SPACE_CHARS = new char[] { ' ', '\t','\n','\r','\f'};
    public static final char[] INDEX_CHARS = new char[] { '@', '#' };
    public static final char[] QUOTE_CHARS = new char[] { '"', '\'' };
    public static final char[] CONTROL_CHARS = new char[] { '\r', '\f' };

	/***************************************************************************
	 * 得到一个6位随机数
	 **************************************************************************/
	public static String getRomNum() {
		Random r=new Random();
		r.nextInt(1);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat fullfmt1 = new SimpleDateFormat("mmss");
		return fullfmt1.format(cal.getTime())+ r.nextInt(9)+r.nextInt(9);
	}

    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static final char[] INVALID_CHARS = new char[] {
        '{', '}','!','@','#', '$', '%', '^', '&', '*','(',')',
        '-', '+', '=','\\', '\'','|', ']', '[', '"',':',';',
        '?', '/', ',', '>', '<', '~', '`', ' ', '\t', '\n','\r','\f'
    };
    
    public static final char[] GSM_CHARS = new char[] {
        '\u0040', '\u00A3', '\u0024', '\u00A5', '\u00E8', '\u00E9', '\u00F9', '\u00EC',
        '\u00F2', '\u00E7', '\n', '\u00D8', '\u00F8', '\r', '\u00C5', '\u00E5',
        '\u0394', '\u005F', '\u03A6', '\u0393', '\u039B', '\u03A9', '\u03A0', '\u03A8',
        '\u03A3', '\u0398', '\u039E', '\u00A0', '\u00C6', '\u00E6', '\u00DF', '\u00C9',
        '\u0020', '\u0021', '\u0022', '\u0023', '\u00A4', '\u0025', '\u0026', '\'',
        '\u0028', '\u0029', '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
        '\u0038', '\u0039', '\u003A', '\u003B', '\u003C', '\u003D', '\u003E', '\u003F',
        '\u00A1', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D', '\u004E', '\u004F',
        '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057',
        '\u0058', '\u0059', '\u005A', '\u00C4', '\u00D6', '\u00D1', '\u00DC', '\u00A7',
        '\u00BF', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
        '\u0068', '\u0069', '\u006A', '\u006B', '\u006C', '\u006D', '\u006E', '\u006F',
        '\u0070', '\u0071', '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u00E4', '\u00F6', '\u00F1', '\u00FC', '\u00E0'
    };
    
    public static String sortStrArrToStr(String[] strArr, String delimiter, final boolean asc){
        Arrays.sort(strArr, new Comparator(){
            public int compare(Object o1, Object o2) {
                int result = ((String)o1).compareTo((String)o2);
                result = (asc)?result:((-1)*result);
                return result;
            }}
        );
        StringBuffer sb = new StringBuffer();
        for(String s: strArr){
            sb.append(s+delimiter);
        }
        String result = sb.substring(0, sb.length()-1);
        return result;
    }
    

    
    public static int extractNumber(String data) {
        StringBuilder b = new StringBuilder();
        char[] cs = data.toCharArray();
        boolean started = false;
        for(int i=0; i<cs.length; ++i) {
            char c = getDigit(cs[i]);
            if (c!='?') {
                started = true;
                b.append(c);
            } else if (started) {
                break;
            }
        }
        if (b.length()==0) return -1;
        return Integer.parseInt(b.toString());
    }
    
    public static class Email {
        public static boolean isEMAIL(String email) {
            return Pattern.matches("[\\p{Alnum},_,.]+@[\\w+\\.]+\\p{Alpha}{2,3}", email);
        }
    }
    
    public static class IP {
        public static boolean isIPAddress(String ip) {
            return Pattern.matches("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)", ip);
        }
    }

    public static String trimValidWMLText(String text) {
        if (text==null || text.trim().length()==0) return "";
        StringBuffer buff = new StringBuffer(text);
        StringUtil.replaceAll("&","",buff);
        StringUtil.replaceAll("'","",buff);
        StringUtil.replaceAll("\"","",buff);
        StringUtil.replaceAll(">","",buff);
        StringUtil.replaceAll("<","",buff);
        StringUtil.replaceAll("$","",buff);
        return buff.toString();
    }
    
    public static void getValidWMLText(StringBuffer buff) {
        if (buff==null) return;
        StringUtil.replaceAll("&","&amp;",buff);
        StringUtil.replaceAll("'","&#39;",buff);
        StringUtil.replaceAll("\"","&#34;",buff);
        StringUtil.replaceAll(">","&#62;",buff);
        StringUtil.replaceAll("<","&#60;",buff);
        StringUtil.replaceAll("$","$$;",buff);
    }
    
    public static String getValidWMLText(String text) {
        if (text==null || text.trim().length()==0) return "";
        StringBuffer buff = new StringBuffer(text);
        StringUtil.replaceAll("&","&amp;",buff);
        StringUtil.replaceAll("'","&#39;",buff);
        StringUtil.replaceAll("\"","&#34;",buff);
        StringUtil.replaceAll(">","&#62;",buff);
        StringUtil.replaceAll("<","&#60;",buff);
        StringUtil.replaceAll("$","$$;",buff);
        return buff.toString();
    }
    
    public static byte[] getUnicode(String str) {
        char[] chars = str.toCharArray();
        byte[] result = new byte[chars.length*2];
        for(int i=0; i<chars.length; ++i) {
            result[2*i] = (byte)(chars[i]/256);
            result[2*i+1] = (byte)(chars[i]%256);
        }
        return result;
    }
    
    public static final String HexCode[] = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "A", "B", "C", "D", "E", "F"
    };
    
    public static byte getGSMCode(char c) {
        if (c>='a'&& c<='z' || c>='A' && c<='Z' || c>='0' && c<='9') return (byte)c;
        for(byte i=0; i<GSM_CHARS.length; ++i) if (c==GSM_CHARS[i]) return i;
        return (byte)0x20;
    }
    
    public static byte[] getGSMCode(String text) {
        char[] data = text.toCharArray();
        byte[] result = new byte[data.length];
        for(int i=0; i<data.length; ++i) result[i] = getGSMCode(data[i]);
        return result;
    }

    public static String getCUCS2String(byte[] data, int offset, int size) {
        try {
            int i;
            size = (size / 2)*2;
            for(i=0; i <size; i+=2) {
                if (data[offset+i]==0 && data[offset+i+1]==0) break;
            }
            String str = new String(data,offset,i,"UTF-16BE");
            return new String(data,offset,i,"UTF-16BE");
        } catch(Exception e) {}
        return null;
    }

    public static String byteArrayToHexString(byte bytes[], int offset, int size) {
    	 StringBuffer sb = new StringBuffer(bytes.length * 2);
    	    for (int i = 0; i < bytes.length; i++) {
    	      sb.append(convertDigitToHexChar(bytes[i] >> 4));
    	      sb.append(convertDigitToHexChar(bytes[i] & 0xF));
    	    }
    	    return sb.toString();
    }

    private static char convertDigitToHexChar(int value)
    {
      value &= 15;
      if (value >= 10) {
        return (char)(value - 10 + 97);
      }
      return (char)(value + 48);
    }

    public static String byteArrayToHexString(byte bytes[]) {
    	 StringBuffer sb = new StringBuffer(bytes.length * 2);
 	    for (int i = 0; i < bytes.length; i++) {
 	      sb.append(convertDigitToHexChar(bytes[i] >> 4));
 	      sb.append(convertDigitToHexChar(bytes[i] & 0xF));
 	    }
 	    return sb.toString();
    }

    public static String byteArrayToHexString2(byte bytes[]) {
   	 StringBuffer sb = new StringBuffer(bytes.length * 2);
	    for (int i = 0; i < bytes.length; i++) {
	      sb.append(convertDigitToHexChar(bytes[i] >> 4));
	      sb.append(convertDigitToHexChar(bytes[i] & 0xF));
	    }
	    return sb.toString();
    }

    public static byte[] hexStringToByteArray(String digits) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < digits.length(); i += 2) {
          char c1 = digits.charAt(i);
          if (i + 1 >= digits.length()) {
            throw new IllegalArgumentException("hexUtil.odd");
          }
          char c2 = digits.charAt(i + 1);
          byte b = 0;
          if ((c1 >= '0') && (c1 <= '9'))
            b = (byte)(b + (c1 - '0') * 16);
          else if ((c1 >= 'a') && (c1 <= 'f'))
            b = (byte)(b + (c1 - 'a' + 10) * 16);
          else if ((c1 >= 'A') && (c1 <= 'F'))
            b = (byte)(b + (c1 - 'A' + 10) * 16);
          else
            throw new IllegalArgumentException("hexUtil.bad");
          if ((c2 >= '0') && (c2 <= '9'))
            b = (byte)(b + (c2 - '0'));
          else if ((c2 >= 'a') && (c2 <= 'f'))
            b = (byte)(b + (c2 - 'a' + 10));
          else if ((c2 >= 'A') && (c2 <= 'F'))
            b = (byte)(b + (c2 - 'A' + 10));
          else
            throw new IllegalArgumentException("hexUtil.bad");
          baos.write(b);
        }
        return baos.toByteArray();
    }
    
    
    public static int hexStringToInt(String text) {
    	return Integer.valueOf(new String(StringUtil.hexStringToByteArray(text)));
    }
    
    public static String hexStringToString(String hexString, String charSet){
        if (hexString==null) return null;
        String result = "";
        try {
            result = new String(hexStringToByteArray(hexString), charSet);
        } catch (Exception ex) {}
        return result;
    }
    
    public static String hexStringToAsciiString(String hexString){
        return hexStringToString(hexString, "ASCII");
    }
    
    public static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return HexCode[d1] + HexCode[d2];
    }
    
    public static Vector tokenize(String data, String[] keyWords) {
        Vector result = new Vector();
        int cursor = 0;
        int length = data.length();
        while(cursor<length) {
            int bestK = -1, bestP = length;
            for(int k=0; k<keyWords.length; ++k) {
                int p = data.indexOf(keyWords[k],cursor);
                if (p>=0) {
                    if (p<bestP) { bestK = k; bestP = p; };
                }
            }
            if (bestK>=0) {
                if (bestP>cursor) {
                    String substring = data.substring(cursor,bestP);
                    result.add(substring);
                }
                result.add(new Integer(bestK));
                cursor = bestP + keyWords[bestK].length();
            } else {
                String substring = data.substring(cursor,length);
                result.add(substring);
                break;
            }
        }
        return result;
    }
    
    public static String serialize(Vector tokens, String[] keyWords) {
        StringBuffer buff = new StringBuffer();
        for(int i=0; i<tokens.size(); ++i) {
            Object token = tokens.elementAt(i);
            if (token instanceof String) {
                buff.append((String)token);
            } else if (token instanceof Integer) {
                int k = ((Integer)token).intValue();
                buff.append(keyWords[k]);
            }
        }
        return buff.toString();
    }
    
    public static void replaceAll(String orignal, String target, StringBuffer data) {
        int i = 0, start=0;
        while(true) {
            i = data.indexOf(orignal,start);
            if (i<0) break;
            data.replace(i,i+orignal.length(), target);
            start = i+target.length();
        }
    }
    
    public static boolean isValidName(String s) { return isWord(s); }
    
    public static char getDigit(char c) {
        if (c>='0' && c<='9') return c;
        if (c>='０' && c<='９') return (char)(c-'０'+'0');
        return '?';
    }
    
    public static boolean isWord(String s) {
        if (s==null) return false;
        if (s.trim().length()==0) return false;
        for(int i=0; i<INVALID_CHARS.length; ++i)
            if (s.indexOf(INVALID_CHARS[i])>=0) return false;
        return true;
    }
    
    public static boolean isASCII(String s) {
        char[] data = s.toCharArray();
        for(int i=0; i<data.length; ++i)
            if (data[i]<0||data[i]>127) return false;
        return true;
    }
    
    public static String getQuotedSubstring(String text, String beginToken, String endToken) {
        if(text==null) return "";
        int pos1=text.indexOf(beginToken);
        if(pos1==-1) return "";
        int pos2=text.indexOf(endToken,pos1+1);
        if(pos2==-1) return "";
        if(pos2<=pos1) return "";
        return text.substring(pos1+beginToken.length(),pos2);
    }
    
    public static String getTruncatedString(String src, String encoding, int length) {
        if (src==null) return null;
        String result = "";
        for(int i=0; i<src.length(); ++i) {
            char c = src.charAt(i);
            try {
                if ((result+c).getBytes(encoding).length>=length) break;
            } catch(Exception ex) {
                ex.printStackTrace();
                return result;
            }
            result += c;
        }
        return result;
    }
    
    public static String getTruncatedString(String src, int length) {
        if (src==null) return null;
        if (src.length() < length) return src;
        return src.substring(0,length);
    }
    
    public static String getFixedLengthString(String src, int length) {
        if (src.length() < length) {
            char[] tail = new char[length - src.length()];
            Arrays.fill(tail,'0');
            return src + new String(tail);
        } else {
            return src.substring(0,length);
        }
    }
    
    static Calendar cal = null;
    
    public static String getTimestamp() {
        return getTimestamp(System.currentTimeMillis(),"// ::.");
    }
    
    public static String getTimestamp(String separators) {
        return getTimestamp(System.currentTimeMillis(),separators);
    }
    
    public static String getTimestamp(long time) {
        return getTimestamp(time,"// ::.");
    }
    
    public static String getTimestamp(long time, String separators) {
        if (cal==null) cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        StringBuffer result = new StringBuffer(cal.get(Calendar.YEAR)+ String.valueOf( separators.charAt(0)));
        result.append((cal.get(Calendar.MONTH)+ 1)+String.valueOf(separators.charAt(1)));
        result.append(cal.get(Calendar.DAY_OF_MONTH) +String.valueOf(separators.charAt(2)));
        result.append(cal.get(Calendar.HOUR_OF_DAY)+String.valueOf(separators.charAt(3)));
        result.append(cal.get(Calendar.MINUTE)+String.valueOf(separators.charAt(4)));
        result.append(cal.get(Calendar.SECOND)+String.valueOf(separators.charAt(5)));
        result.append(cal.get(Calendar.MILLISECOND));
        return result.toString();
    }
    
    public static int countSimilarChars(String s1, String s2) {
        int l1 = s1.length(), l2 = s2.length(), i;
        int size = l1<l2 ? l1 : l2;
        for(i=0; i<size; ++i) {
            if (s1.charAt(i)!=s2.charAt(i)) break;
        }
        return i;
    }
    
    public static String[] splitWords(String words, String regex) {
        String[] terms = words.split(regex);
        Vector<String> result = new Vector<String>();
        for(int i=0; i<terms.length; ++i)
            if (terms[i].length()>0) result.add(terms[i]);
        String[] data = new String[result.size()];
        return (String[])result.toArray(data);
    }
    
    public static String[] splitWords(String words) {
        return splitWords(words,"[\\p{Blank}\\p{Punct}]");
    }
    
    public static int head(int start, byte[] data, byte[] separator) {
        int state = 0;
        for(int i=start; i<data.length; ++i) {
            if (data[i]==separator[state]) state++;
            if (state==separator.length) return i+1-separator.length;
        }
        return -1;
    }
    
    public static Vector<byte[]> split(byte[] data, byte[] separator) {
        Vector<byte[]> result = new Vector<byte[]>();
        int start = 0;
        while(start<data.length) {
            int pos = head(start, data,separator);
            if(pos!=-1) {
                byte[] term = new byte[pos-start];
                System.arraycopy(data,start,term,0,term.length);
                result.add(term);
                start += term.length + separator.length;
            } else {
                byte[] term = new byte[data.length-start];
                System.arraycopy(data,start,term,0,term.length);
                result.add(term);
                start += term.length + separator.length;
            }
        }
        return result;
    }
    
    public static void split(Vector<String> tem, String str, String flag, int index){
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<str.length();){
            if(compare(str,flag,i)){
                i=i+flag.length();
                String sc = builder.toString();
                tem.add(sc);
                builder= new StringBuilder();
            }else{
                builder.append(str.substring(i,i+2)) ;
                i=i+2;
            }
        }
        if(!builder.toString().equals("")){
            tem.add(builder.toString());
        }
    }
    
    public static boolean compare(String str ,String flag,int start){
        if((start+flag.length())>str.length())
            return false;
        else{
            String tem = str.substring(start,start+flag.length());
            if(tem.equals(flag))
                return true;
            else
                return false;
        }
    }
    public static String GBKToISO(String content){
        if(content == null){ return ""; }
        try {
            return new String(content.getBytes("gb2312"),"iso8859-1");
        } catch (UnsupportedEncodingException ex) {
            return content;
        }
    }
    
    public static String[] parseArray(String data, String separators) { 
        return parseArray(data,separators.charAt(0),separators.charAt(1),separators.charAt(2),separators.charAt(3));
    }
    
    public static String[] parseArray(String data, char open, char comma, char close, char quote) { 
        Vector<String> result = new Vector<String>();
        int state = 0;
        StringBuilder buff = new StringBuilder();
        char[] all = data.trim().toCharArray(); 
        boolean inQuote = false; 
        for(int i=0; i<all.length; ++i) {
            char c = all[i];
            if (inQuote) { 
                if (c==quote) inQuote = false; 
                buff.append(c); 
            } else { 
                if (c==quote) { 
                    inQuote = true;
                    buff.append(c);
                } else if (c==open) {  
                    if (state>0) buff.append(c);
                    state++; 
                } else if (c==close) { 
                    state--;
                    if (all.length-1==i && buff.length()>0) { 
                        result.add(buff.toString());
                    } else buff.append(c);
                } else if (c==comma) { 
                    if (state==1 && buff.length()>0) { 
                        result.add(buff.toString());
                        buff = new StringBuilder(); 
                    } else if (state>0) { 
                        buff.append(c);
                    }
                } else buff.append(c);
            }
        }
        return (String[])result.toArray(new String[result.size()]); 
    }
   
    /**
	 * 用指定的字符填充指定的字符串达到指定的长度，并返回填充之后的字符串<br>
	 * 
	 * @param p_scr
	 *            待填充的字符串
	 * @param p_fill
	 *            填充的字符
	 * @param p_length
	 *            填充之后的字符串总长度
	 * @param direction
	 *            填充方向，SerialPart.FRONT 前面，SerialPart.BACK后面
	 * @return String 填充之后的字符串
	 */
	public static String fill(String p_scr, char p_fill, int p_length,
			int direction) {
		/* 如果待填充字符串的长度等于填充之后字符串的长度，则无需填充直接返回 */
		if (p_scr.length() == p_length) {
			return p_scr;
		}
		/* 初始化字符数组 */
		char[] fill = new char[p_length - p_scr.length()];
		/* 填充字符数组 */
		Arrays.fill(fill, p_fill);
		/* 根据填充方向，将填充字符串与源字符串进行拼接 */
		switch (direction) {
		case FRONT:
			return String.valueOf(fill).concat(p_scr);
		case BACK:
			return p_scr.concat(String.valueOf(fill));
		default:
			return p_scr;
		}
	}
	//判断汉字个数
	public static int checkChineseCharacterCount(String str) {   
	    int ccCount = 0;   
	    String regEx = "[\\u4e00-\\u9fa5]";   
	    Pattern p = Pattern.compile(regEx);
	    java.util.regex.Matcher m = p.matcher(str);   
	    while (m.find()) {   
	        for (int i = 0; i <= m.groupCount(); i++) {   
	            ccCount = ccCount + 1;   
	        }   
	    }   
	    return ccCount;   
	}  
		
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
				.toString().substring(1) : temp.toString();
	}
    
	
	public static String fillBytes_GBK(String p_scr, char p_fill, int bytes_p_length,
			int direction) {
		/* 如果待填充字符串的长度等于填充之后字符串的长度，则无需填充直接返回 */
		try {
			if (p_scr.getBytes("GBK").length == bytes_p_length) {
				return p_scr;
			}
			/* 初始化字符数组 */
			char[] fill = new char[bytes_p_length - p_scr.getBytes("GBK").length];
			/* 填充字符数组 */
			Arrays.fill(fill, p_fill);
			/* 根据填充方向，将填充字符串与源字符串进行拼接 */
			switch (direction) {
			case FRONT:
				return String.valueOf(fill).concat(p_scr);
			case BACK:
				return p_scr.concat(String.valueOf(fill));
			default:
				return p_scr;
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 字符串转ascII码
	 * @param value
	 * @return
	 */
	public static String stringToAscii(String value)  
	{  
	    StringBuffer sbu = new StringBuffer();  
	    char[] chars = value.toCharArray();   
	   for (int i = 0; i < chars.length; i++) {  
	        if(i != chars.length - 1)  
	       {  
	            sbu.append((int)chars[i]).append("");  
	       }  
	        else {  
	           sbu.append((int)chars[i]);  
	       }  
	    }  
	    return sbu.toString();  
	}

    public static String leftFillZero(long num, int len)
    {
        NumberFormat nf = NumberFormat.getInstance();

        nf.setGroupingUsed(false);

        nf.setMaximumIntegerDigits(len);

        nf.setMinimumIntegerDigits(len);

        return nf.format(num);
    }

    public static String fillStrOfZero(String string, int totalLength)
    {
        int currentLength = string.getBytes().length;
        int delta = totalLength - currentLength;
        for (int i = 0; i < delta; i++) {
            string = '0' + string;
        }
        return string;
    }

    public static String addRightZero(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() > length) {
            return value.substring(0, length - 1);
        }
        char[] c = new char[length];
        System.arraycopy(value.toCharArray(), 0, c, 0, value.length());
        for (int i = value.length(); i < c.length; i++) {
            c[i] = '0';
        }
        return new String(c);
    }

    public static String addLeftZero(String s, int length)
    {
        int old = s.length();
        if (length > old) {
            char[] c = new char[length];
            char[] x = s.toCharArray();
            if (x.length > length) {
                throw new IllegalArgumentException(
                        "Numeric value is larger than intended length: " + s +
                                " LEN " + length);
            }
            int lim = c.length - x.length;
            for (int i = 0; i < lim; i++) {
                c[i] = '0';
            }
            System.arraycopy(x, 0, c, lim, x.length);
            return new String(c);
        }
        return s.substring(0, length);
    }
}
