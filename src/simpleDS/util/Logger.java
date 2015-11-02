/* @description This class implements reusable methods for logging information.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.util;


public class Logger {

	public static void debug(String className, String method, String message) {
		System.out.println("[DEB] " + className + "." + method + "(): " + message);
	}

	public static void info(String className, String method, String message) {
		System.out.println("[INF] " + className + "." + method + "(): " + message);
	}

	public static void warning(String className, String method, String message) {
		System.out.println("[WAR] ------------------------------------------------------------------");
		System.out.println("[WAR] " + className + "." + method + "(): " + message);
		System.out.println("[WAR] ------------------------------------------------------------------");
	}

	public static void error(String className, String method, String message) {
		System.out.println("[ERR] ******************************************************************");
		System.out.println("[ERR] " + className + "." + method + "(): " + message);
		System.out.println("[ERR] ******************************************************************");
		System.exit(0);
	}
}
