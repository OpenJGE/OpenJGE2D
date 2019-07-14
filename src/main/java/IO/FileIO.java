package IO;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * The <code>FileIO</code> class is used for performing all input and output operations to and from files.
 */
public class FileIO {

    /**
     * Loads the entire contents of a file and stores it in a <code>String</code>. Useful for when accessing source
     * code such as shader files.
     *
     * @param fileName The location of the file to be loaded
     * @return The string containing the loaded source code
     */
    public String loadCodeFile(String fileName) {
        String output;
        // A try-with-resources statement is used so that any resources are automatically closed after the try statement
        // executes. In the case that an exception were to occur in both the try statement and a hypothetical finally
        // clause (used, in this case, to manually close the input stream), the first exception (which we care about,
        // since it tells us what went wrong) is lost, as the second exception suppresses it. However, the try-with-
        // resources statement prevents this, as a .close() exception would simply be attached to the first exception
        try (InputStream input = Class.forName(FileIO.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(input, StandardCharsets.UTF_8)) {
            // The "\A" delimiter is used to token the entire text file string. An additional backslash is added since
            // a single backslash is used as an escape in any string (?)
            output = scanner.useDelimiter("\\A").next();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not load code file:" + e);
        }
        return output;
    }

}
