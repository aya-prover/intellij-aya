import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.aya.intellij.actions.search.ProofSearch

class ProofSearchParserTest : BasePlatformTestCase() {
  fun `test wildcard matches any id`() = "_".matches("hello")
  fun `test wildcard matches any group`() = "_".matches("(a + b)")
  fun `test question matches any id`() = "?".matches("hello")
  fun `test question does not match any group`() = "?".doesNotMatch("(a + b)")
  fun `test id matches full text`() = "hell".doesNotMatch("hello")
  fun `test paren group`() = "a + (? + ?)".matches("a + (b + c)")
  fun `test +-comm`() = "? + ? = ? + ?".matches("a + b = b + a")
  fun `test +-assoc`() = "? + (? + ?) = (? + ?) + ?".matches("a + (b + c) = (a + b) + c")

  private fun String.matches(test: String) = matches(test, true)
  private fun String.doesNotMatch(test: String) = matches(test, false)
  private fun String.matches(test: String, success: Boolean) {
    val expr = ProofSearch.parse(project, this).rightValue
    val regex = ProofSearch.compile(0, expr)
    TestCase.assertEquals(success, regex.toPattern().matcher(test).matches())
  }
}
