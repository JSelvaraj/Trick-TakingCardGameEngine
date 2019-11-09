import build.Shuffle;
import org.junit.runner.JUnitCore;
import test.ExampleTest;

// import org.junit.runner.Result;
// import org.junit.runner.notification.Failure;

public class TestRunner {

    static Class[] testClasses = {
            ExampleTest.class,
            Shuffle.class
    };

    public static void main(String[] args) {
        JUnitCore.runClasses(testClasses);
    }
}
