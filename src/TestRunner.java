import org.junit.runner.JUnitCore;
// import org.junit.runner.Result;
// import org.junit.runner.notification.Failure;

import test.*;

public class TestRunner {

  static Class[] testClasses = {
    ExampleTest.class
  };

  public static void main(String[] args) {
    JUnitCore.runClasses(testClasses);
  }
}
