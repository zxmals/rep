 

package tdh.frame.common;
 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
import tdh.frame.cache.FacadeCacheUtils;
import tdh.frame.common.DBUtils;
import tdh.frame.system.TUser;
import tdh.frame.system.TsBzdm;
import tdh.frame.system.dao.TsBzdmDao;
import tdh.frame.web.context.WebAppContext;
import tdh.frame.web.util.SpringBeanUtils;
import tdh.frame.xtgl.TDepart;
import tdh.frame.xtgl.dao.TUserRoleDAO;
import tdh.frame.xtgl.service.TDepartService;
 
public class UtilComm {
 
    /** 日志. */
    private static final Log log = LogFactory.getLog(UtilComm.class);
 
    /** 流程相关：处理标志. */
    public static Map<String, String> clbzMap = new HashMap<String, String>();
 
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
    private static final double MAX_VALUE = 9999999999999.99D;
    private static final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
    public static final String STR_EMPTY = "";
 
    /**
     * 初始化处理标志数据结构.
     */
    static {
       clbzMap.put("1", "待");
       clbzMap.put("2", "办");
       clbzMap.put("3", "结");
       clbzMap.put("4", "退");
       clbzMap.put("5", "止");
       clbzMap.put("6", "未");
       clbzMap.put("7", "撤");
    }
 
    /**
     * 获取当天是星期几.
     * @return 日、一、二、....六
     */
    public static String getDayOfUpWeek(){
       Calendar cal = Calendar.getInstance();
       int w = cal.get(Calendar.DAY_OF_WEEK);
       if(w == 1){
           return "日";
       }else if(w == 2){
           return "一";
       }else if(w == 3){
           return "二";
       }else if(w == 4){
           return "三";
       }else if(w == 5){
           return "四";
       }else if(w == 6){
           return "五";
       }else if(w == 7){
           return "六";
       }
       return "";
    }
    
    /**
     * 用户代码翻译成姓名.
     * @param yhdm 用户代码
     * @return 用户姓名,如果没有查找到，返回空串
     */
    public static String getYhName(String yhdm) {
       if(isEmpty(yhdm)) return STR_EMPTY;
       return convertString(FacadeCacheUtils.convertYhxm(yhdm));
    }
 
    /**
     * 用户代码翻译成姓名，同上.
     * @param yhdm 用户代码
     * @return 用户姓名,如果没有查找到，返回空串
     */
    public static String getYhxm(String yhdm) {
       if(isEmpty(yhdm)) return STR_EMPTY;
       return convertString(FacadeCacheUtils.convertYhxm(yhdm));
    }
 
    /**
     * 用户代码翻译成姓名，同上.
     * @param yhdm 用户代码
     * @return 用户姓名,如果没有查找到，返回空串
     */
    public static String getYhmc(String yhdm) {
       if(isEmpty(yhdm)) return  STR_EMPTY;
       return convertString(FacadeCacheUtils.convertYhxm(yhdm));
    }
 
    /**
     * 多个逗号相隔的用户代码串的翻译成姓名串，指定分隔符串联，如,hyx,lxx,dtx.
     * @param name 用户代码串，如,hyx,lxx,dtx,
     * @param fgf 返回值中的分隔符，一般是逗号，可以是其他符号
     * @return 用户姓名串，如：小二,小东,小明
     */
    public static String convertNames(String name, String fgf) {
       if(isEmpty(name)) return  STR_EMPTY;
       name = UtilComm.convertString(name);
       if (name.startsWith(",")) {
           name = name.substring(1);
       }
       if (name.endsWith(",")) {
           name = name.substring(0, name.length() - 1);
       }
       String temp = "";
       StringBuffer sbrtn = new StringBuffer();
       if (name.indexOf("dept") != -1) {
           name = name.substring(4);
           sbrtn.append(getBmmc(name));
       } else {
           String[] names = name.split(",");
           for (String n : names) {
              if(isEmpty(n)) continue;
              temp = FacadeCacheUtils.convertYhxm(n);
              if (temp == null) {
                  sbrtn.append(fgf + n);
              } else {
                  sbrtn.append(fgf + temp);
              }
           }
           if (sbrtn.length() > 0) {
              sbrtn.delete(0, 1);
           }
       }
       return sbrtn.toString();
    }
 
    /**
     * 多个逗号相隔的用户代码串的翻译成姓名串，逗号串联，如,hyx,lxx,dtx.
     * @param name 用户代码串，如,hyx,lxx,dtx,
     * @return 用户姓名串，如：小二,小东,小明
     */
    public static String convertNames(String name) {
       return convertNames(name, ",");
    }
 
    /**
     * 部门代码翻译.
     * @param bmdm 部门代码
     * @return 部门名称,如果没有查找到，返回空串
     */
    public static String getBmmc(String bmdm) {
       if(isEmpty(bmdm)) return  STR_EMPTY;
       return convertString(FacadeCacheUtils.convertBmmc(bmdm));
    }
 
    /**
     * 用户所在部门的名称.
     * @param yhdm
     * @return 例如：立案庭
     */
    public static String getYhbmmc(String yhdm) {
       if(isEmpty(yhdm)) return  STR_EMPTY;
       TUser user = FacadeCacheUtils.getTUser(yhdm);
       if (user == null || isEmpty(user.getYhbm())) {
           return STR_EMPTY;
       }
       return getBmmc(user.getYhbm());
    }
 
    /**
     * 用户所在部门的名称，同上.
     * @param yhdm
     * @return 例如：立案庭
     */
    public static String getYhbm(String yhdm) {
       return getYhbmmc(yhdm);
    }
 
    /**
     * 获取用户代码所在的部门代码
     * @param yhdm
     * @return 例如：32010001
     */
    public static String getYhbmdm(String yhdm){
       if(isEmpty(yhdm)) return  STR_EMPTY;
       TUser user = FacadeCacheUtils.getTUser(yhdm);
       if (user == null || isEmpty(user.getYhbm())) {
           return STR_EMPTY;
       }
       return user.getYhbm();
    }
 
    /**
     * 角色翻译 T_ROLE.
     * @param jsdm 角色代码
     * @return 角色名称,如果没有查找到，返回空串
     */
    public static String getJsmc(String jsdm) {
       if(isEmpty(jsdm)) return STR_EMPTY;
       return convertString(FacadeCacheUtils.convertJsmc(jsdm));
    }
 
    /**
     * 标准代码翻译 TS_BZDM.
     * @param bmdm 标准代码
     * @return 标准代码名称,如果没有查找到，返回空串
     */
    public static String getBzdm(String bzdm) {
       if(isEmpty(bzdm)) return  STR_EMPTY;
       return convertString(FacadeCacheUtils
              .convertTsBzdm(UtilComm.convertString(bzdm)));
    }
 
    /**
     * 标准代码翻译 TS_BZDM，同上.
     * @param bmdm 标准代码
     * @return 标准代码名称,如果没有查找到，返回空串
     */
    public static String convertBzdm(String bzdm){
       return getBzdm(bzdm);
    }
 
    /**
     * 翻译法院代码到完整的名称.
     * @param fydm 法院代码
     * @return 320100 --> 南京市中级人民法院
     */
    public static String getFymc(String fydm){
       if(isEmpty(fydm)) return STR_EMPTY;
       return convertString(FacadeCacheUtils.convertFymc(fydm));
    }
 
    /**
     * 翻译法院代码到代字.
     * @param fydm 法院代码
     * @return 320100 --> 宁
     */
    public static String getFyjc(String fydm){
       if(isEmpty(fydm)) return  STR_EMPTY;
       return convertString(FacadeCacheUtils.convertFyjc(fydm));
    }
 
    /**
     * 翻译法院代码到短称.
     * @param fydm 法院代码
     * @return 例如：320100 --> 南京中院
     */
    public static String getFydc(String fydm){
       if(isEmpty(fydm)) return STR_EMPTY;
       return convertString(FacadeCacheUtils.convertFydc(fydm));
    }
 
    /**
     * 转换字符串为Double型，空串或"."返回Double(0).
     * @param val 待转换的字符串
     * @return Double值
     */
    public static Double converStrToDouble(String val) {
       if (val == null) {
           val = "";
       } else {
           val = val.trim();
       }
       if ("".equals(val) || ".".equals(val)) {
           return Double.valueOf(0);
       } else {
           return Double.parseDouble(val);
       }
    }
 
    /**
     * 处理传入字符串，如果为null，返回空串，否则去掉首尾空格.
     * @param val 待处理参数
     * @return String
     */
    public static String convertString(String val) {
       if (val == null) {
           return "";
       } else {
           return val.trim();
       }
    }
 
    /**
     * 转换Double类型为字符串输出，保留2位小数，直接输出字符串数字太多时会显示为科学读数法.
     * @param val 数值
     * @return 字符串.
     */
    public static String convertDoubleToStr(Double val) {
       if (val == null || val >= -0.0001 && val <= 0.0001) {
           return "";
       } else {
           return new DecimalFormat("0.00").format(val);
       }
    }
 
    /**
     * 数据库decimal类型数据以Object保存在Map中，将其转为字符串输出，保留2位小数.
     * @param val 数值对象
     * @return 字符串.
     */
    public static String convertDoubleToStr(Object val) {
       if (val == null) {
           return "";
       }
       if (val instanceof BigDecimal) {
           BigDecimal dec = (BigDecimal) val;
           if (dec.doubleValue() <= 0.0001) {
              return "";
           } else {
              return new DecimalFormat("0.00").format(dec);
           }
       } else {
           return "";
       }
    }
 
    /**
     * 去掉案件表DSRC中的换行符，换行符在JSON格式数据列表显示时有问题.
     * @param dsrc 当事人串
     * @return String
     */
    public static String convertDsrc(String dsrc) {
       if (dsrc == null) {
           return "";
       }
       dsrc = dsrc.replaceAll("\r", "");
       dsrc = dsrc.replaceAll("\n", "");
       return dsrc;
    }
 
    /**
     * 去掉参数的首尾空格，并且在参数为空串或null的情况下返回“0”.
     * @param val 待处理的值
     * @return 去掉首尾空格的串或“0”值
     */
    public static String convertNulltoZero(String val) {
       if (val == null) {
           val = "0";
       } else {
           val = val.trim();
       }
       if (val.equals("")) {
           val = "0";
       }
       return val;
    }
 
    /**
     * 时间类型格式化.
     * @param dt 日期类型的变量
     * @param fmt 例："yyyy-MM-dd"，"yy/MM/dd"等，尽量使用"yyyy-MM-dd"格式
     * @return 根据指定的fmt格式的日期串
     */
    public static String dateFormat(Date dt, String fmt) {
       if (dt == null)
           return "";
       SimpleDateFormat sdf = new SimpleDateFormat(fmt);
       return sdf.format(dt);
    }
 
    /**
     * 字符串日期格式化. 针对 varchar(8)的字符日期.
     * @param date 日期，yyyyMMdd 形式，如 20100803.
     * @param sp 分割符号，一般为 "-" 或者 "/"
     * @return String 返回 如 "2010-08-03" 或者 "2010/08/03".
     */
    public static String dateStrFormat(String date, String sp) {
       if (date == null)
           return "";
       if (date.length() < 8)
           return date;
       return date.substring(0, 4) + sp + date.substring(4, 6) + sp
              + date.substring(6);
    }
 
    /**
     * 将数据库中取出的Date类型返回 yyyy-MM-dd 的日期字符串返回.
     * @param rq 日期
     * @return MM-dd HH:mm字符串，例如02-14 10:49
     */
    public static String convertRq4(Date rq) {
       SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
       if (rq == null) {
           return "";
       } else {
           return sdf.format(rq);
       }
    }
 
    /**
       * 将日期格式化成19位，yyyy-MM-dd HH:mm:ss格式
       * @param rq1 日期串，yyyyMMdd 或 yyyy-MM-dd HH:mm:ss:mmm
       * @return yyyy-MM-dd HH:mm:ss格式，输入8位时时间部分补00:00:00
       */
      public static String convertRq5(String rq1) {
        String rq = "";
        if (rq1 == null) return "";else rq1 = trim(rq1);
        if (!"".equals(rq1) && rq1.length() == 8) {
          rq = rq1.substring(0, 4) + "-" + rq1.substring(4, 6) + "-" + rq1.substring(6, 8) + " 00:00:00";
        } else if(!"".equals(rq1) && rq1.length() >= 19) {
        rq = rq1.substring(0,19);
        }
        return rq;
      }
 
    /**
     * 将数据库中取出的Date类型返回 yyyy-MM-dd 的日期字符串返回.
     * @param rq 日期
     * @return String yyyy-MM-dd字符串
     * 注：暂未发现使用。
     */
    public static String convertRq6(String rq) {
       if (rq == null) {
           return "";
       } else {
           if (rq.length() >= 16) {
              return rq.substring(5, 10) + " " + rq.substring(11, 16);
           }
           return "";
       }
    }
 
    /**
     * 日志查询页面的时间转换（返回毫秒时间） yyyy-MM-dd HH:mm:ss:SSS
     * @param rq 日期
     * @return yyyy-MM-dd HH:mm:ss:SSS
     */
    public static String convertRqforRz(Date rq) {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
       if (rq == null) {
           return "";
       } else {
           return sdf.format(rq);
       }
    }
 
      /**
       * 20100101转换为 2010年 01月 01日
       * @param rq1 yyyymmdd，例如20100101
       * @return 2010年 01月 01日
       */
      public static String convertRq10(String rq1) {
        String rq = "";
        if (rq1 == null) rq1 = ""; else rq1 = trim(rq1);
        if (!"".equals(rq1) && (rq1.length() > 7)) {
          rq = rq1.substring(0, 4) + "年 " + rq1.substring(4, 6) + "月 " + rq1.substring(6) + "日";
        }
        return rq;
      }
 
      /**
       * 保留日期的前16位信息，一般用于去除秒这个时间
       * @param str  时间串 格式：2012-12-22 12:22:10
       * @return 返回：2012-12-22 12:22
       */
    public static String convertRq16(String str) {
       if (str == null) {
           str = "";
       }
       if (str.length() > 16) {
           return str.substring(0, 16);
       }
       return str;
    }
 
      /**
       * 将yyyy-mm-dd转换成指定分隔符格式.
       * @param rq1  2009-01-01
       * @param seprator 分隔符，为空或/等
       * @return rq2 20090101 等格式
       */
      public static String convertRq1(String rq1, String seprator) {
        String rq2 = "";
        if (rq1 == null || "".equals(rq1)) {
          return "";
        }
        rq2 = rq1.split("-")[0] + seprator + rq1.split("-")[1] + seprator + rq1.split("-")[2];
        return rq2;
      }
 
      /**
       * 将yyyy-mm-dd转换成yyyymmdd格式
       * @param rq1  2009-01-01
       * @return rq2 20090101
       */
      public static String convertRq1(String rq1) {
         return UtilComm.convertRq1(rq1,"");
      }
 
      /**
       * 将yyyymmdd转换成yyyy-mm-dd格式
       * @param rq1，yyyymmdd格式
       * @return yyyy-mm-dd格式
       */
      public static String convertRq2(String rq1) {
        String rq = "";
        if (rq1 == null) return "";else rq1 = trim(rq1);
        if (!"".equals(trim(rq1))&&(rq1.length() == 8)) {
          rq = rq1.substring(0, 4) + "-" + rq1.substring(4, 6) + "-"  + rq1.substring(6, 8);
        }
        return rq;
      }
 
      /**
       * 将yyyymmdd转换成yy-mm-dd格式
       * @param rq1，yyyymmdd格式
       * @return yy-mm-dd格式
       */
      public static String convertRq3(String rq1) {
        String rq = "";
        if (rq1 == null) rq1 = ""; else rq1 = trim(rq1);
        if (!"".equals(trim(rq1))&&(rq1.length() > 7 )) {
          rq = rq1.substring(2, 4) + "-" + rq1.substring(4, 6) + "-"  + rq1.substring(6);
        }
        return rq;
      }
 
    /**
     * 将数据库中取出的Date类型返回 yyyy-MM-dd 的日期字符串返回.
     * @param rq 日期
     * @return String yyyy-MM-dd字符串
     */
    public static String convertRq(Date rq) {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       if (rq == null) {
           return "";
       } else {
           return sdf.format(rq);
       }
    }
 
    /**
       * 日期转换成指定格式
       * @param rq
       * @param format
       * @return 由format指定格式的字符串
       */
      public static String convertRq(Date rq, String format) {
        if (rq == null)
          return "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(rq);
      }
 
    /**
     * 转换yyyy-mm-dd字符型日期字符串为指定分隔符格式，和convertRq1相同，建议取消.
     * @param rq1  2009-01-01
     * @param seprator 分隔符，为空或/等
     * @return rq2 20090101或2009/01/01等格式
     */
    public static String convertRqFormat(String rq1, String seprator) {
       String rq2 = "";
       if (rq1 == null || "".equals(rq1))
           return "";
       rq2 = rq1.split("-")[0] + seprator + rq1.split("-")[1] + seprator
              + rq1.split("-")[2];
       return rq2;
    }
 
    /**
     * 转换"yyyy-MM-dd"格式的字符串为Date类型，如果参数为空或是null，返回null.
     * @param val "yyyy-MM-dd"格式的字符串
     * @return Date或null
     */
      public static Date convertStrtoDate(String val) {
           try {
             if (val != null && val.length() == 10) {
               return new SimpleDateFormat("yyyy-MM-dd").parse(val);
             } else  if (val != null && val.length() == 8) {
               return new SimpleDateFormat("yyyyMMdd").parse(val);
             } else{
               return null;
             }
           } catch (Exception e) {
             log.error(e.getMessage(), e);
             return null;
           }
         }
 
    /**
     * 计算相对日期，日期yyyymmdd格式，增加天数后，返回yyyymmdd格式字符串
     * @param date，yyyymmdd格式
     * @param aa，天数，可以是负数
     * @return yyyymmdd格式字符串
     */
    public static String addDate(String date, int aa) {
       try {
           SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
           sdf.setLenient(false);
           Date datee = null;
           Calendar calendar1 = new GregorianCalendar();
           DecimalFormat df = new DecimalFormat("00");
           datee = sdf.parse(date);
           calendar1.setTime(datee);
           calendar1.set(Calendar.DATE, calendar1.get(Calendar.DATE) + aa);
           String date2 = (calendar1.get(Calendar.YEAR))
                  + df.format(calendar1.get(Calendar.MONTH) + 1)
                  + df.format(calendar1.get(Calendar.DATE));
           return date2;
       } catch (Exception e) {
           e.printStackTrace();
       }
       return date;
    }
 
    /**
     * 转换yyyy-MM-dd HH:mm:ss格式的日期为yyyy-MM-dd，即前十位.
     * @param rq yyyy-MM-dd HH:mm:ss形式的字符
     * @return yyyy-MM-dd形式的字符串，如果rq为空，返回空串
     */
    public static String getRq(String rq) {
       if (rq != null && rq.length() >= 10) {
           return rq.substring(0, 10);
       } else {
           return "";
       }
    }
 
    /**
     * 处理数字，页面显示0值一般不显示，因此如果为0值，返回空串，否则返回转化其字符串.
     * @param val 待处理参数
     * @return String
     */
    public static String convertZero(Integer val) {
       if (val == null || val == 0) {
           return "";
       } else {
           return "" + val;
       }
    }
 
    /**
     * 将Short,Integer,BigDecimal,Double类型值为0和null的转为空字符,用于页面显示.
     * @param val 待处理参数
     * @return String
     */
      public static String convertZero(Object val) {
           if (val instanceof String) {
             String str = (String) val;
             if (val == null || "0".equals(str) || "null".equals(str))
               return "";
             else
               return trim(str);
           } else if (val instanceof Integer) {
             Integer i = (Integer) val;
             if (i == null || i.intValue() == 0) {
               return "";
             } else {
               return "" + i;
             }
           } else if (val instanceof Short) {
             Short s = (Short) val;
             if (s == null || s.shortValue() == 0) {
               return "";
             } else {
               return "" + s;
             }
           } else if (val instanceof BigDecimal) {
             BigDecimal bd = (BigDecimal) val;
             if (bd == null)
               return "";
             Double dou = bd.doubleValue();
             dou = round(dou, 2);
             if (Math.abs(dou) < 0.0001) {
               return "";
             } else {
               DecimalFormat df = new DecimalFormat("#0.00");
               return df.format(bd);
             }
           } else if (val instanceof Double) {
             Double dou = (Double) val;
             dou = round(dou, 2);
             if (dou == null || Math.abs(dou.doubleValue()) < 0.0001) {
               return "";
             } else {
               DecimalFormat df = new DecimalFormat("#0.00");
               return df.format(dou);
             }
           } else {
             return "";
           }
         }
 
      /**
       * 保留几位小数
       * @param v 数值
       * @param scale 进度 4舍5入
       * @return 按照指定精度的小数
       */
      public static double round(double v, int scale) {
        if (scale < 0) {
          throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
      }
 
       /**
        * 测试传入字符串是否合法的数字，true/false
        * @param cond 测试的字符串
        * @return true如果是数字，否则false
        */
       public static boolean isDouble(String cond) {
           if (cond == null || cond.trim().equals("")) {
              return false;
           }
           try {
              Double.parseDouble(cond.trim());
              return true;
           } catch (Exception e) {
              return false;
           }
       }
 
       /**
        * 转大写金额
        * @param v 金额
        * @return 大写金额字符串，20140214.0 --> 贰千零壹拾肆万零贰佰壹拾肆元整
        */
       public static String doubleToCnBig(Double v) {
 
           if (v == null || v < 0 || v > MAX_VALUE)
              return "";
           long l = Math.round(v * 100);
           if (l == 0)
              return "零元整";
           String strValue = l + "";
           //i用来控制数
           int i = 0;
           //j用来控制单位
           int j = UNIT.length() - strValue.length();
           StringBuffer rs = new StringBuffer();
           boolean isZero = false;
           for (; i < strValue.length(); i++, j++) {
              char ch = strValue.charAt(i);
 
              if (ch == '0') {
                  isZero = true;
                  if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万'
                         || UNIT.charAt(j) == '元') {
                     rs = rs.append(UNIT.charAt(j));
                     isZero = false;
                  }
              } else {
                  if (isZero) {
                     rs = rs.append("零");
                     isZero = false;
                  }
                  rs = rs.append(DIGIT.charAt(ch - '0')).append(UNIT.charAt(j));
              }
           }
 
           if (!rs.toString().endsWith("分")) {
              rs = rs.append("整");
           }
           String returnVal = rs.toString().replaceAll("亿万", "亿");
           return returnVal;
       }
 
       /**
        * TRIM 将字符串对象去空格或者NULL对象变""
        * @param str String对象.
        * @return trim() 2010.7.23 增加判断 字符串 "null"也返回空串
        */
       public static String trim(String str) {
           if (str == null || "null".equals(str)) {
              str = "";
           }
           return str.trim();
       }
 
       /**
        * 判断字符串是否为空
        * @param str  字符串
        * @return true/false
        */
       public static boolean isEmpty(String str) {
           if (str == null)
              return true;
           if ("".equals(str.trim()))
              return true;
           return false;
       }
 
         /**
         * 处理String，避免  “  ‘ 破坏json 数据结构
         * @param str
         * @return  字符串
         */
        public static String conJsonStr(String str){
            if(str == null)
            str = "";
            str = str.replace("\"","\\\"");
            str = str.replace("\'","\\\'");
            return str;
        }
 
     /**
      * 移除字符串内的"/"的结尾信息和头部带"/"的字符
      * 一般在URL地址的处理
      * @param path 地址
      * @return 新的地址。例如传入　http://127.0.0.1/court/ 返回 http://127.0.0.1/court
      */
      public static String removeslash(String path){
         if(path.endsWith("/")){
             path = path.substring(0, path.length()-1);
         }
         if(path.startsWith("/")){
             path = path.substring(1);
         }
         return path;
      }
 
  /**
     * 对编码的参数进行解码，统一使用UTF-8解码，因此编码时也应使用UTF-8.
     * @param val 字符串参数
     * @return 解码后的结果
     */
    public static String decode(String val) {
       if (val == null) {
           return "";
       }
       try {
           return URLDecoder.decode(val.trim(), "UTF-8");
       } catch (UnsupportedEncodingException e) {
           return "";
       }
    }
 
    /**
     * 对编码的参数进行编码，统一使用UTF-8编码，因此解码时也应使用UTF-8.
     * @param val  字符串参数
     * @return 编码码后的结果
     */
    public static String encode(String val) {
       if (val == null) {
           return "";
       }
       try {
           return URLEncoder.encode(val.trim(), "UTF-8");
       } catch (UnsupportedEncodingException e) {
           return "";
       }
    }
 
    /**
     * 对编码的参数进行编码，统一使用UTF-8编码，因此解码时也应使用UTF-8.
     * @param val  字符串参数
     * @return 编码码后的结果
     */
     public static String encodeURI(String str) {
         return encode(str);
      }
 
     /**
        * 对编码的参数进行解码，统一使用UTF-8解码，因此编码时也应使用UTF-8.
        * @param val 字符串参数
        * @return 解码后的结果
        */
      public static String decodeURI(String str) {
         return decode(str);
      }
 
      /**
       * HTTP协议传参时,以UTF-8解析
       * 获取request中的参数信息
       * @param name 参数名
       * @param request  请求
       * @return 参数值（默认解码一次）
       */
      public static String getPar8(String name, HttpServletRequest request) {
        String val = trim(request.getParameter(name));
        try {
          if (val == null || "null".equals(val)){
        val = "";
          }else{
            val = java.net.URLDecoder.decode(val, "UTF-8");
          }
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          val = "";
        }
        return val;
      }
 
      /**
       * 获取HttpReuqest请求中的参数信息，并且将其转换成Map
       * @param request 请求
       * @return 请求的参数结构集
       */
      public static Map<String,String> getHttpRquestDataMap(HttpServletRequest request){
       Map<String,String> dataMap = new HashMap<String,String>();
       java.util.Enumeration<?>  emu= request.getParameterNames();
       while(emu.hasMoreElements()){
           String key = (String) emu.nextElement();
           String value = decodeURI(request.getParameter(key));
           dataMap.put(key, value);
       }
       return dataMap;
      }
 
    /**
     * 翻译处理标志：1,2,3,4,5 为 办，结，退，止.
     * @param clbz 处理标志:范围：1,2,3,4,5
     * @return 处理标志汉字，范围：办，结，退，止
     */
    public static String getClbzmc(String clbz) {
       return clbzMap.get(clbz);
    }
 
    /**
     * 根据表名转化为JavaBean名称，如SFTJ_FZB30_ZXAJ：SftjFzb30Zxaj.
     * @param tableName 表名
     * @return String 转化后的类对象名称
     */
    public static String getJavaBeanName(String tableName) {
       String tName = tableName;
       if (UtilComm.convertString(tName).equals("")) {
           return "";
       }
       String[] bms = tName.split("_");
       StringBuffer sb = new StringBuffer();
       for (String bm : bms) {
           sb.append(bm.substring(0, 1) + bm.substring(1).toLowerCase());
       }
       return sb.toString();
    }
 
    /**
     * 校验替换字符串内的非法字符
     * @param str 输入字符串
     * @return 字符串
     */
     public static String validate1(String str){
           return str.replaceAll("(^|\\&)|(\\|)|(\\;)|(\\$)|(\\%)|(\\@)|(\\')|(\\\")|(\\>)|(\\<)|(\\))|(\\()|(\\+)|(\\,)|(\\\\)|(\\#|$)","");
    }
 
     /**
      * 校验SQL语句，去除头部带"'"号的非法语句
      * @param str SQL脚本
      * @return  SQL脚本
      */
     public static String validateSql(String str){
        int p1 = str.indexOf("'");
        if (p1 >= 0){
            if (p1 == 0){
               str = "";
            }else{
               str = str.substring(0,p1);
            }
        }
        return str;
     }
 
     /**
        * 基于SYBASE数据库的分页算法（使用临时表方式）
        * 
        * @param conn
        *            数据库连接
        * @param cols
        *            列信息 例如：col1,col2,col3
        * @param cond
        *            查询条件 from EAJ where AHDM=?
        * @param start
        *            起始行
        * @param limit
        *            每页数量
        * @return 返回一个List<Map>结果集
        * @throws SQLException
        */
    public static List<Map<String, Object>> generateList(Connection conn,
           String cols, String cond, int start, int limit) throws SQLException {
 
       PreparedStatement st = null;
       ResultSet rs = null;
       List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
       String dbtype = WebAppContext.getWebConfig().getDbType();
 
       if ("oracle".equals(dbtype)) {
           try {
              int orderIndex = cond.toUpperCase().indexOf(" ORDER "); 
               
              int total = 0, cnum = 0; 
              if (orderIndex > 0){ 
              st = conn.prepareStatement("select count(*)" + cond.substring(0, orderIndex)); 
              } else { 
              st = conn.prepareStatement("select count(*)" + cond); 
              } 
              rs = st.executeQuery();
              if (rs.next())
                  total = rs.getInt(1);
              Map<String, Object> map = new HashMap<String, Object>();
              map.put("total", Integer.valueOf(total));
              list.add(map);
              
              start = start == 0 ? 1 : start; 
               
              String query = "select * from (  " 
              + " select page.*,rownum rn from (select " + cols + " " 
              + cond + ") page " + " where rownum < " 
              + (start + limit) + ") where rn >= " + start; 
              
              st = conn.prepareStatement(query);
              rs = st.executeQuery();
 
              ResultSetMetaData rsdata = rs.getMetaData();
              cnum = rsdata.getColumnCount();
              while (rs.next()) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     if("rn".equalsIgnoreCase(rsdata.getColumnName(i))) continue;
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
           } catch (Exception e) {
              e.printStackTrace();
           } finally {
              DBUtils.close(null, null, st, rs);
           }
       } else {
           try {
              conn.setAutoCommit(true);
 
              String temp_table = "tempdb..A_" + System.currentTimeMillis()
                     + "_temp_paging";
              int total = 0, cnum = 0;
              String newsql = "";
              newsql = "select " + cols + ",TEMPDB_ID=IDENTITY(10) into "
                     + temp_table + " " + cond;
              // 初始化，生成临时表及数据
              st = conn.prepareStatement(newsql);
              st.execute();
 
              // 查询总记录数
              st = conn.prepareStatement("select count(TEMPDB_ID) from "
                     + temp_table);
              rs = st.executeQuery();
              if (rs.next())
                  total = rs.getInt(1);
 
              // 返回结果集的第一个记录放置总数
              Map<String, Object> map = new HashMap<String, Object>();
              map.put("total", Integer.valueOf(total));
              list.add(map);
 
              // 从临时表中查询数据
              if (limit == 0) { // 显示全部数据:
                                // 注意：显示全部数据情况下，dhtmlx解析速度非常慢，因此限定最大记录数为500
                  newsql = "select * from " + temp_table
                         + " where TEMPDB_ID <= 500";
              } else {
                  newsql = "select * from " + temp_table
                         + "  where TEMPDB_ID>=" + start
                         + " and TEMPDB_ID <" + (start + limit);
              }
              st = conn.prepareStatement(newsql);
               rs = st.executeQuery();
              ResultSetMetaData rsdata = rs.getMetaData();
              cnum = rsdata.getColumnCount();
              // 将结果集放到List中
              while (rs.next()) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
              // 删除临时表
              st = conn.prepareStatement("drop table " + temp_table);
              st.execute();
 
              conn.setAutoCommit(false);
           } catch (Exception e) {
              e.printStackTrace();
           } finally {
              DBUtils.close(null, null, st, rs);
           }
       }
       return list;
    }
 
    /**
     * 通用分页算法，没有传递数据库连接时使用，将返回第start条到第end条记录，结果存放在List中.
     * 
     * @param sql
     *            完整的查询条件
     * @param start
     *            开始记录
     * @param limt
     *            记录数
     * @return 结果集：List<br/>
     *         注意：结果集第一条记录为合计，key为total，其余为记录集，一个Map代表一个条记录，Map中的key为各字段名
     */
    public static List<Map<String, Object>> getResultList(String sql,
           int start, int limit) {
       Connection conn = null;
       Statement st = null;
       ResultSet rs = null;
       try {
           conn = WebAppContext.getFrameConn();
           return UtilComm.getResultList(conn, st, rs, sql, start, limit);
       } finally {
           DBUtils.close(conn, st, null, rs);
       }
    }
 
    /**
     * 分页查询，查询语句分开写
     * @param cols 查询列
     * @param condition 查询条件
     * @param start  开始记录
     * @param limit 记录数
     * @return 记录数
     */
    public static List<Map<String, Object>> getResultList(String cols,
           String condition, int start, int limit) {
       Connection conn = null;
       try {
           conn = WebAppContext.getFrameConn();
           return UtilComm.generateList(conn, cols, condition, start,
                  limit);
       } catch (Exception e) {
           log.error(e.getMessage(), e);
           return null;
       } finally {
           DBUtils.close(conn, null, null, null);
       }
    }
 
    /**
     * 通用分页算法，传递数据库连接时使用，将返回第start条到第end条记录，结果存放在List中.
     * @param sql 完整的查询条件
     * @param start 开始记录
     * @param limit 记录数
     * @return 结果集：List<br/>
     *  注意：结果集第一条记录为合计，key为total，其余为记录集，一个Map代表一个条记录，Map中的key为各字段名
     */
    public static List<Map<String, Object>> getResultList(Connection conn,
           Statement st, ResultSet rs, String sql, int start, int limit) {
       String cols = "", cond = "";
       try {
           sql = sql.trim();
           cols = sql.substring(6, sql.toLowerCase().indexOf("from"));
           cond = sql.substring(sql.toLowerCase().indexOf("from"));
           return UtilComm
                  .generateList(conn, cols, cond, start, limit);
       } catch (Exception e) {
           log.error(e.getMessage(), e);
           return null;
       }
    }
 
    /**
     * 通用分页算法，没有传递数据库连接时使用，将返回第start条到第end条记录，结果存放在List中.适用与特殊的sql语句
     * @param sql 完整的查询条件
     * @param start 开始记录
     * @param limt 记录数
     * @return 结果集：List<br/>
     *  注意：结果集第一条记录为合计，key为total，其余为记录集，一个Map代表一个条记录，Map中的key为各字段名
     */
    public static List<Map<String, Object>> getResultListRs(String sql,
           int start, int limit) {
       Connection conn = null;
       Statement st = null;
       ResultSet rs = null;
       try {
           conn = WebAppContext.getFrameConn();
           return UtilComm.getResultListRs(conn, st, rs, sql, start, limit);
       } finally {
           DBUtils.close(conn, st, null, rs);
       }
    }
    
    
 
    /**
     * 通用分页算法，传递数据库连接时使用，将返回第start条到第end条记录，结果存放在List中.适用与特殊的sql语句
     * @param sql 完整的查询条件
     * @param start 开始记录
     * @param limit 记录数
     * @return 结果集：List<br/>
     *  注意：结果集第一条记录为合计，key为total，其余为记录集，一个Map代表一个条记录，Map中的key为各字段名
     */
    public static List<Map<String,Object>> getResultListRs(Connection conn,String sql,
           int start, int limit){
       List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
       int total = 0, cnum = 0;
       Statement st = null;
       ResultSet rs = null;
       try {
           sql = sql.trim();
           st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_READ_ONLY);
           rs = st.executeQuery(sql);
           // 总记录数
           rs.last();
           total = rs.getRow();
           Map<String, Object> map = new HashMap<String, Object>();
           map.put("total", Integer.valueOf(total));
           list.add(map);
           ResultSetMetaData rsdata = rs.getMetaData();
           cnum = rsdata.getColumnCount();
           if (limit == 0) { // 显示全部数据
              // 将结果集放到List中
              rs.absolute(1);
              rs.previous();
              while (rs.next()) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
           } else {
              rs.absolute(start);
              rs.previous();
              // 将结果集放到List中
              while (rs.next() && rs.getRow() < (start + limit)) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
           }
       } catch (Exception e) {
           log.error(e.getMessage(), e);
       } finally {
           DBUtils.close(conn, st, null, rs);
       }
       return list;
    }
 
    /**
     * 通用分页算法，传递数据库连接时使用，将返回第start条到第end条记录，结果存放在List中.适用与特殊的sql语句
     * @param sql 完整的查询条件
     * @param start 开始记录
     * @param limit 记录数
     * @return 结果集：List<br/>
     *  注意：结果集第一条记录为合计，key为total，其余为记录集，一个Map代表一个条记录，Map中的key为各字段名
     */
    public static List<Map<String, Object>> getResultListRs(Connection conn,
           Statement st, ResultSet rs, String sql, int start, int limit) {
       List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
       int total = 0, cnum = 0;
       try {
           sql = sql.trim();
           st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                  ResultSet.CONCUR_READ_ONLY);
           rs = st.executeQuery(sql);
           // 总记录数
           rs.last();
           total = rs.getRow();
           Map<String, Object> map = new HashMap<String, Object>();
           map.put("total", Integer.valueOf(total));
           list.add(map);
           ResultSetMetaData rsdata = rs.getMetaData();
           cnum = rsdata.getColumnCount();
           if (limit == 0) { // 显示全部数据
              // 将结果集放到List中
              rs.absolute(1);
              rs.previous();
              while (rs.next()) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
           } else {
              rs.absolute(start);
              rs.previous();
              // 将结果集放到List中
              while (rs.next() && rs.getRow() < (start + limit)) {
                  map = new HashMap<String, Object>();
                  for (int i = 1; i <= cnum; i++) {
                     map.put(rsdata.getColumnName(i), rs.getObject(i));
                  }
                  list.add(map);
              }
           }
       } catch (Exception e) {
           log.error(e.getMessage(), e);
       }
       return list;
    }
 
      /**
       * 解压文件流
       * @param in 输入流
       * @return 返回一个字节输出流
       */
      public static ByteArrayOutputStream deCompress(InputStream in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipInputStream zipIn = null;
        try {
          CheckedInputStream csumi = new CheckedInputStream(in, new CRC32());
          zipIn = new ZipInputStream(new BufferedInputStream(csumi));
          byte[] bytes = new byte[1024];
          while ((zipIn.getNextEntry()) != null) {
            int x;
            while ((x = zipIn.read(bytes)) != -1) {
              baos.write(bytes, 0, x);
            }
          }
          csumi.close();
        } catch (Exception e1) {
          e1.printStackTrace();
        }finally{
        try{
            if (zipIn != null)
                zipIn.close();
        }catch (IOException e1){
            e1.printStackTrace();
        }
        }
        return baos;
      }
 
      /**
       * 压缩文件流
       * @param in 输入流
       * @param name 文件名（压缩后在压缩文件的名称）
       * @return
       */
      public static ByteArrayOutputStream compress(InputStream in, String name) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream out = null;
        try {
          CheckedOutputStream csum = new CheckedOutputStream(baos, new CRC32());
          out = new ZipOutputStream(new BufferedOutputStream(csum));
          out.putNextEntry(new ZipEntry(name));
          byte[] bytes = new byte[1024];
          int c;
          while ((c = in.read(bytes)) != -1) {
            out.write(bytes, 0, c);
          }
          out.flush();
          csum.flush();
          csum.close();
        } catch (Exception e1) {
          e1.printStackTrace();
        }finally{
        try{
            if (out != null)
                out.close();
        }catch(IOException e1){
            e1.printStackTrace();
        }
        }
        return baos;
      }
 
      /**
       * 代码进行了封装统一，业务逻辑调整到WebAppContext 中
       * @param yydm
       * @return 应用的URL地址 例如：http://127.0.0.1/court
       */
      public static String getAppURL(String yydm){
         return WebAppContext.getTAppUrl(yydm);
      }
 
      /**
       * 获取部门下拉列表.
       * @param withSpace 是否以空记录开始.
       * @param sfjy 是否考虑禁用：true表示取未禁用的本院部门；false表示取所有的本院部门
       * @return option组成的串 2010.7.23 增加参数selected，
       */
 
      public static String getBMSignleFyOp(boolean withSpace, String selected, boolean sfjy) {
        TDepartService tDepartService = (TDepartService) WebAppContext.getBeanEx("TDepartService");
        StringBuilder opStr = new StringBuilder();
        String bmdm = null;
        String bmmc = null;
        String sfselect = null;
        try {
          List<TDepart> list = tDepartService.findTDepartBySingleFy("", sfjy);
          if (withSpace) {
            opStr.insert(0, "<option value=''></option>");
          }
          for (TDepart tDepart : list) {
            bmdm = trim(tDepart.getBmdm());
            bmmc = trim(tDepart.getBmmc());
            if (selected.equals(bmdm)) {
              sfselect = "selected";
            } else {
              sfselect = "";
            }
            opStr.append("<option value='" + bmdm + "' " + sfselect + ">");
            opStr.append(bmmc);
            opStr.append("</option>\n");
          }
        } catch (Exception e) {
        }
        return opStr.toString();
      }
 
      /**
       * 获取部门信息
       * @param fydm 法院代码
       * @param bmdm 部门代码（第一层部门bmdm=fydm）
       * @param fymc 法院名称
       * @return 部门List
       */
      public static List<TDepart> getBmtree(String fydm, String fymc, HttpServletRequest request) {
        Connection conn = null;
        PreparedStatement psmt = null;
        ResultSet rs = null;
        List<TDepart> list = new ArrayList<TDepart>();
        String sql = "";
        try {
          conn = SpringBeanUtils.getConn(request);
          sql = "select BMMC,BMDM,'0',FBMDM from T_DEPART where isnull(SFJY, '0') <> '1' and DWDM='"+fydm+"' " + " union "
              + " select YHXM,YHBM+'$'+YHID,'1',YHBM from T_USER where isnull(SFJY, '0') <> '1' and DWDM='"+fydm+"'order by BMDM";
          psmt = conn.prepareStatement(sql);
          rs = psmt.executeQuery();
          while (rs.next()) {
            TDepart td = new TDepart();
            String bmdm = rs.getString(2);
            String bmmc = rs.getString(1);
            String sflast = rs.getString(3);
            String fbmdm = rs.getString(4);
            td.setBmdm(bmdm);
            td.setBmmc(bmmc);
            td.setSflast(sflast);
            td.setFbmdm(fbmdm);
            list.add(td);
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          DBUtils.close(conn, psmt, rs);
        }
        return list;
      }
 
      /**
       * 获取一个标准代码的TS_BZDM表的数据.
       * @param kind 代码类型.
       * @param include05 是否包含05的代码
       * @return List<TsBzdm>
       */
      public static List<TsBzdm> getBzdmList(String kind, boolean include05) {
           TsBzdmDao dao = (TsBzdmDao) WebAppContext.getBeanEx("FrameTsBzdmDao");
           return dao.findByKind(kind, include05);
      }
 
      /**
       * 获取一个标准代码的TS_BZDM表的数据.
       * @param kind 代码类型.
       * @param order 排序 eg:order by mc
       * @return List<TsBzdm>
       */
      public static List<TsBzdm> getBzdmList(String kind, String order) {
        TsBzdmDao dao = (TsBzdmDao) WebAppContext.getBeanEx("FrameTsBzdmDao");
        return dao.findByKind(kind, order);
      }
 
      /**
       * 获取一个标准代码的TS_BZDM表的数据.
       * @param kind 代码类型.
       * @param include05 是否包含05的代码
       * @param order 排序 eg:order by mc
       * @return List<TsBzdm>
       */
      public static List<TsBzdm> getBzdmList(String kind, String order, boolean include05) {
        TsBzdmDao dao = (TsBzdmDao) WebAppContext.getBeanEx("FrameTsBzdmDao");
        return dao.findByKind(kind, order, include05);
      }
 
      /**
       * 获取一个标准代码的TS_BZDM表的数据.
       * @param kind 代码类型.
       * @param cond 附件条件（过滤条件)
       * @param include05 是否包含05的代码
       * @param order 排序 eg:order by mc
       * @return List<TsBzdm>
       */
      public static List<TsBzdm> getBzdmListByCond(String kind, String cond, String order, boolean include05) {
        TsBzdmDao dao = (TsBzdmDao) WebAppContext.getBeanEx("FrameTsBzdmDao");
        return dao.findByCond(kind, cond, order, include05);
      }
 
      /**
       * 获取下拉对象TS_BZDM.
       * @param kind 代码类型.
       * @param selected 已选择对象.
       * @param emptyOption 是否有空值.
       * @return option元素集
       */
      public static String getBzdmOption(String kind, String selected, boolean emptyOption) {
        return getBzdmOption(kind,selected,emptyOption,false);
      }
 
      /**
       * 获取下拉对象TS_BZDM.
       * @param kind
       * @param selected
       * @param emptyOption
       * @param include05 是否包含05
       * @return  option元素集
       */
      public static String getBzdmOption(String kind, String selected, boolean emptyOption, boolean include05) {
        StringBuffer sb = new StringBuffer();
        if (emptyOption) {
          sb.append("<option value=\"\"></option>");
        }
        List<TsBzdm> list;
        if ("00004".equals(kind)) { //国籍按名称排序
          list = getBzdmList(kind, " order by mc ASC", include05);
        } else {
          list = getBzdmList(kind, include05);
        }
        for (TsBzdm bzdm : list) {
          if (bzdm.getId().getCode().trim().equals(selected)) {
            sb.append("<option value=\"" + bzdm.getId().getCode() + "\"  selected>" + bzdm.getMc() + "</option>");
          } else {
            sb.append("<option value=\"" + bzdm.getId().getCode() + "\" >" + bzdm.getMc() + "</option>");
          }
        }
        return sb.toString();
      }
 
      /**
       * 获得一家法院，某一角色所有人员代码及姓名下拉列表.
       * @param fydm 法院代码
       * @param jsdm 角色代码
       * @param selected 哪个用户被选中
       * @param withSpace 是否增加空的选择项
       * @return 某角色的人员列表.
       */
     public static String getRoleUsers(String fydm, String jsdm, String selected, boolean withSpace) {
        TUserRoleDAO tUserRoleDAO = (TUserRoleDAO) WebAppContext.getBeanEx("TUserRoleDAO");
        return tUserRoleDAO.getRoleUserOpt(fydm, jsdm, selected, withSpace);
      }
 
      /**
       * 从缓存表中获取对应的下拉框列表（效率较高-推荐)
       * 仅框架部分的标准代码，建议业务系统也实现一个类似的缓存类来减轻压力。
       * @param kind 类型
       * @return option元素集
       */
      public static String getBzdmOptFromCache(String kind,String selected,boolean empty){
         List<TsBzdm> list = FacadeCacheUtils.getListTsBzdm(kind);
         StringBuilder opt = new StringBuilder();
         if(empty){
             opt.append("<option></option>");
         }
         for(TsBzdm bzdm : list){
             opt.append("<option value=\"");
             opt.append(bzdm.getId().getCode());
             if(bzdm.getId().getCode().equals(selected)){
                opt.append("\" selected>");
             }else{
                opt.append("\">");
             }
             opt.append(bzdm.getMc());
             opt.append("</option>");
         }
         return opt.toString();
      }
 
      /**
        * 获取部门的下拉列表.
        * @param defValue 默认选中值，如果为空，不选中
        * @param withSpace 是否以空行开头
        * @return String option元素集
        */
       public static String getBmOptions(String defValue, boolean withSpace) {
           StringBuilder bf = new StringBuilder();
           Connection conn = null;
           Statement st = null;
           ResultSet rs = null;
           try {
              conn = WebAppContext.getFrameConn();
              st = conn.createStatement();
              rs = st.executeQuery("select BMDM,BMMC FROM T_DEPART order by PXH,BMDM");
              String extra = "";
              if (withSpace) {
                  bf.append("<option></option>");
              }
              while (rs.next()) {
                  if (defValue.equals(rs.getString("BMDM"))) {
                     extra = " selected";
                  } else {
                     extra = "";
                  }
                  bf.append("<option value=\"").append(rs.getString("BMDM"))
                         .append("\"").append(extra).append(">").append(
                                rs.getString("BMMC")).append("</option>");
              }
           } catch (Exception e) {
              log.error(e.getMessage(), e);
              return "";
           } finally {
              DBUtils.close(conn, st, rs);
           }
           return bf.toString();
       }
 
       /**
        * 获取部门的下拉列表.
        * @param defValue 默认选中值，如果为空，不选中
        * @param withSpace 是否以空行开头
        * @return String option元素集
        * 增加是否禁用条件。20140509周小伟
        */
       public static String getBmOptionsByFydm(String fydm, String defValue,
              boolean withSpace) {
           StringBuilder bf = new StringBuilder();
           Connection conn = null;
           Statement st = null;
           ResultSet rs = null;
           try {
              conn = WebAppContext.getFrameConn();
              st = conn.createStatement();
              rs = st.executeQuery("select BMDM,BMMC FROM T_DEPART where DWDM = '"
                            + fydm + "' and SFJY = '0' order by PXH,BMDM");
              String extra = "";
              if (withSpace) {
                  bf.append("<option></option>");
              }
              while (rs.next()) {
                  if (defValue.equals(rs.getString("BMDM"))) {
                     extra = " selected";
                  } else {
                     extra = "";
                  }
                  bf.append("<option value=\"").append(rs.getString("BMDM"))
                         .append("\"").append(extra).append(">").append(
                                rs.getString("BMMC")).append("</option>");
              }
           } catch (Exception e) {
              log.error(e.getMessage(), e);
              return "";
           } finally {
              DBUtils.close(conn, st, rs);
           }
           return bf.toString();
       }
 
       /**
        * 生成标准代码的多选结构.
        * @param kind 标准代码种类
        * @return HTML格式字符串
        * @deprecated 语法错误，现在的BZDM中，code前都有版本号，所以这个的排序是错误的，审判中未找到调用。20140930周小伟
        */
       
       public static String getMultiSelection(String kind) {
           StringBuilder bf = new StringBuilder();
           Connection conn = null;
           Statement st = null;
           ResultSet rs = null;
           String ords = "";
           try {
              conn = WebAppContext.getFrameConn();
              st = conn.createStatement();
              if (kind.length() == 4) {
                  ords = " order by convert(integer,substring(CODE,6,2))";
              } else if (kind.length() == 3) {
                  ords = " order by convert(integer,substring(CODE,5,2))";
              }
              rs = st.executeQuery("select CODE,MC FROM TS_BZDM WHERE KIND='"
                     + kind + "' AND CODE<>KIND AND SFJY='0' " + ords);
              while (rs.next()) {
                  bf.append("<tr><td width=30 align=center>");
                  bf.append("<input type=checkbox id=\"").append(
                         rs.getString("CODE")).append("\">").append("</td>");
                  bf.append("<td><label for=\"").append(rs.getString("CODE"))
                         .append("\">").append(rs.getString("MC")).append(
                                "</label></td></tr>");
              }
           } catch (Exception e) {
              log.error(e.getMessage(), e);
           } finally {
              DBUtils.close(conn, st, rs);
           }
           return bf.toString();
       }
 
       /**
        * 根据部门代码返回该部门下所有用户的option元素.
        * @param withSpace 是否以空行开头
        * @param bmdm 部门代码
        * @return 用户信息  option元素集
        * 禁用的人员排除在外；排序使用姓名全拼。20140508周小伟
        */
       public static String getYhxxByBmdm(boolean withSpace, String bmdm) {
           HttpSession session = WebAppContext.getCurrentSession();
           UserBean user = (UserBean) session.getAttribute("user");
           String fydm = user.getFy().getFydm();
 
           StringBuilder bf = new StringBuilder();
           Connection conn = null;
           Statement st = null;
           ResultSet rs = null;
           try {
              conn = WebAppContext.getFrameConn();
              st = conn.createStatement();
              if (isEmpty(bmdm)) {
                  rs = st.executeQuery("select YHDM,YHXM FROM T_USER WHERE DWDM='"
                                + fydm + "' and SFJY = '0' order by XMQP");
              } else {
                  rs = st.executeQuery("select YHDM,YHXM FROM T_USER WHERE YHBM='"
                                + bmdm
                                + "' AND DWDM='"
                                + fydm
                                + "' and SFJY = '0' order by XMQP");
              }
              if (withSpace) {
                  bf.append("<option></option>");
              }
              while (rs.next()) {
                  bf.append("<option value='" + rs.getString("YHDM") + "'>"
                         + rs.getString("YHXM") + "</option>");
              }
              return bf.toString();
           } catch (Exception e) {
              log.error(e.getMessage(), e);
              return "";
           } finally {
              DBUtils.close(conn, st, rs);
           }
       }
 
       /**
        * 获取下拉列表.
        * @param sql 完整sql语句，查询两个字段分别代表select选项的值与显示文本，
        *  如：select CODE,MC FROM Table_Name where KIND in ('001','002')。
        * @param defValue 默认选中值，如果为空，不选中
        * @param withSpace 是否以空行开头
        * @return String option元素集
        */
       public static String getCommOptions(String sql, String defValue,
              boolean withSpace) {
           StringBuilder bf = new StringBuilder();
           Connection conn = null;
           Statement st = null;
           ResultSet rs = null;
           String code = "";
           String mc = "";
           try {
              conn = WebAppContext.getFrameConn();
              st = conn.createStatement();
              rs = st.executeQuery(sql);
              String extra = "";
              if (withSpace) {
                  bf.append("<option></option>");
              }
              while (rs.next()) {
                  if (defValue.equals(rs.getString(1))) {
                     extra = " selected";
                  } else {
                     extra = "";
                  }
                  code = UtilComm.convertString(rs.getString(1));
                  mc = UtilComm.convertString(rs.getString(2));
                  if ("".equals(code) || "".equals(mc)) {
                     continue;
                  }
                  bf.append("<option value=\"").append(code).append("\"").append(
                         extra).append(">").append(mc).append("</option>");
              }
           } catch (Exception e) {
              log.error(e.getMessage(), e);
              return "";
           } finally {
              DBUtils.close(conn, st, rs);
           }
           return bf.toString();
       }
 
     /**
      * 获取本机IP的地址信息.
      * @return 例如：127.0.0.1，如果有多个则返回一个数组
      */
    @SuppressWarnings("unchecked")
    public static List<String> getHostIP() {
       List<String> ips = new ArrayList<String>();
       try {
           Enumeration allNetInterfaces;
           allNetInterfaces = NetworkInterface.getNetworkInterfaces();
           InetAddress ip = null;
           while (allNetInterfaces.hasMoreElements()) {
              NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
                     .nextElement();
              Enumeration addresses = netInterface.getInetAddresses();
              while (addresses.hasMoreElements()) {
                  ip = (InetAddress) addresses.nextElement();
                  if (ip != null && ip instanceof Inet4Address) {
                     ips.add(ip.getHostAddress());
                  }
              }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return ips;
    }
    
    /**
     * 将一个字符串划分成一个数组.
     * @param str 字符串
     * @param split 分隔符
     * @return String[]
     */
    public static List<String> split(String str,String split){
        List<String>  list  = new  ArrayList<String>();
       if(str == null || "".equals(str.trim())) return list;
       String[] arr = str.split("\\"+split);
       for(String s : arr){
           if(isEmpty(s)) continue;
           list.add(trim(s));
       }
       return list;
    }
    
    
    /**
     * 将给定的字符串拼接成一个可以在SQL语句中使用IN来查询的条件.
     * @param str  字符串
     * @param split 风格符
     * @param isStr 是否是字符类型数据。
     * @return
     */
    public static String  buildInCond(String str,String split,boolean isStr){
       List<String> list = split(str,split);
       return buildInCond(list,isStr);
    }
    
    /**
     * 将给定的字符串拼接成一个可以在SQL语句中使用IN来查询的条件.
     * @param list 数组 数据
     * @param isStr 是否是字符类型数据。
     * @return
     */
    public static String buildInCond(List<String> list,boolean isStr){
       StringBuilder  cond = new StringBuilder();
       for(String s : list){
           if(cond.length()>0) cond.append(",");
           if(isStr){
              cond.append("'").append(s).append("'");
           }else{
              cond.append(s);
           }
       }
       return cond.toString();
    }
 
    
    /**
     * 获得一年前的日期.
     * @param fgf 分隔符
     * @return
     */
     public static String lastYear(String fgf){
        Date date = new Date();
        int year=Integer.parseInt(new SimpleDateFormat("yyyy").format(date))-1;
        int month=Integer.parseInt(new SimpleDateFormat("MM").format(date));
        int day=Integer.parseInt(new SimpleDateFormat("dd").format(date));
 
        if(month==0){
         year-=1;month=12;
        }
        else if(day>28){
         if(month==2){
          if(year % 100 == 0||(year % 4 == 0 && year % 100 != 0)){
          day=29;
          }else day=28;
         }
        }
        String y = year+"";String m ="";String d ="";
        if(month<10) m = "0"+month;
        else m=month+"";
        if(day<10) d = "0"+day;
        else d = day+"";
      
        return y+fgf+m+fgf+d;
     }
 
      /** 
       *  分析传递的sql语句，将convert识别出来
       *  然后将第一个参数大写，并且两端加单引号
       *  根据第一个参数的内容，将函数名称变为tdh.convertXxx，其中Xxx为Str/Num/Date
       *  分析第一个参数时，需要考虑存在括弧的情况，例如convert(numeric(15,0),LYQSH)
       *  将datediff的第一个参数两侧加单引号
       */
      public static String convertSql(String sqlStr){
       String dbtype = WebAppContext.getWebConfig().getDbType();
//      System.out.println(sqlStr);
//      System.out.println("-->");
       if (dbtype.equals("sybase")){
           return sqlStr;
       }
        int p1=0,p2=0,p3=0,p4=0; //convert(位置，“(”位置，“)”位置，“，”位置
        String param1="";     //第一个参数
        String ctype="";      //参数类型
        p1 = sqlStr.toLowerCase().indexOf("convert(");
        while (p1 > 0){
          p2 = sqlStr.indexOf("(",p1 + 9);
          p4 = sqlStr.indexOf(",",p1 + 9);
          if (p2 > 0 && p2 < p4){
          p3 = sqlStr.indexOf(")",p2 + 1);
          p4 = sqlStr.indexOf(",",p3 + 1);
          }
          param1 = sqlStr.substring(p1 + 8,p4);
          param1 = param1.toUpperCase();
 
          if (param1.startsWith("CHAR") || param1.startsWith("VARCHAR")){
          ctype = "Char";
          }
          if (param1.startsWith("DATE")){
        ctype = "Date";
          }
          if (param1.startsWith("DECIMAL") || param1.startsWith("INT") || param1.startsWith("NUMERIC") || param1.equals("SMALLINT")){
        ctype = "Num";
          }
          //第一参数两侧增加单引号
          sqlStr = sqlStr.substring(0,p1 + 8) + "'" + sqlStr.substring(p1 + 8,p4) + "'" + sqlStr.substring(p4);
          //替换函数名称tdh.convertXxx(
          if (dbtype.equals("db2")){
          sqlStr = sqlStr.substring(0,p1) + "convert" + ctype + sqlStr.substring(p1 + 7);
          }else{
          sqlStr = sqlStr.substring(0,p1) + "tdh.convert" + ctype + sqlStr.substring(p1 + 7);
          }
          p1 = sqlStr.toLowerCase().indexOf("convert(");
        }
//      System.out.println(sqlStr);
//      System.out.println("-----------------------------------------------------");
        //处理datediff
        p1 = sqlStr.toLowerCase().indexOf("datediff(");
        while (p1 > 0){
          p2 = sqlStr.indexOf("(",p1 + 10);
          p4 = sqlStr.indexOf(",",p1 + 10);
          if (p2 > 0 && p2 < p4){
          p3 = sqlStr.indexOf(")",p2 + 1);
          p4 = sqlStr.indexOf(",",p3 + 1);
          }
          param1 = sqlStr.substring(p1 + 8,p4);
          param1 = param1.toUpperCase();
 
          //第一参数两侧增加单引号
          sqlStr = sqlStr.substring(0,p1 + 9) + "'" + sqlStr.substring(p1 + 9,p4) + "'" + sqlStr.substring(p4);
          p1 = sqlStr.toLowerCase().indexOf("datediff(",p1 + 10);
        }
        
        //处理dateadd,20141216，第一个参数增加单引号
        p1 = sqlStr.toLowerCase().indexOf("dateadd(");
        while (p1 > 0){
          p2 = sqlStr.indexOf("(",p1 + 9);
          p4 = sqlStr.indexOf(",",p1 + 9);
          if (p2 > 0 && p2 < p4){
          p3 = sqlStr.indexOf(")",p2 + 1);
          p4 = sqlStr.indexOf(",",p3 + 1);
          }
          param1 = sqlStr.substring(p1 + 8,p4);
          param1 = param1.toUpperCase();
 
          //第一参数两侧增加单引号
          sqlStr = sqlStr.substring(0,p1 + 8) + "'" + sqlStr.substring(p1 + 8,p4) + "'" + sqlStr.substring(p4);
          p1 = sqlStr.toLowerCase().indexOf("dateadd(",p1 + 9);
        }
        return sqlStr;
      }
}
 