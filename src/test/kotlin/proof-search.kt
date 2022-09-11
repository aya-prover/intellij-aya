import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.aya.intellij.proof.ProofSearch

class ProofSearchParserTest : BasePlatformTestCase() {
  fun `test wildcard matches any id`() = "_".matches("hello")
  fun `test wildcard does not match group`() = "_".doesNotMatch("(a + b)")
  fun `test id matches full text`() = "hell".doesNotMatch("hello")
  fun `test paren group`() = "a + (_ + _)".matches("a + (b + c)")
  fun `test +-comm`() = "_ + _ = _ + _".matches("a + b = b + a")
  fun `test +-assoc`() = "_ + (_ + _) = (_ + _) + _".matches("a + (b + c) = (a + b) + c")

  private fun String.matches(test: String) = matches(test, true)
  private fun String.doesNotMatch(test: String) = matches(test, false)
  private fun String.matches(test: String, success: Boolean) {
    val expr = ProofSearch.parse(this).rightValue
    val regex = ProofSearch.compile(0, expr)
    TestCase.assertEquals(success, regex.toPattern().matcher(test).matches())
  }
}
