package org.aya.intellij.inspection;

import kala.collection.SeqView;
import kala.collection.mutable.MutableSet;
import org.aya.util.reporter.Problem;
import org.jetbrains.annotations.NotNull;

public final class Jobs {
  private final @NotNull MutableSet<Class<? extends Problem>> NOT_MY_JOB = MutableSet.create();

  /**
   * meme: https://hinative.com/en-US/questions/19531228
   *
   * @apiNote always call this method from class initializers
   */
  public void passMe(@NotNull Class<? extends Problem> problemType) {
    synchronized (NOT_MY_JOB) {
      NOT_MY_JOB.add(problemType);
    }
  }

  public @NotNull SeqView<Problem> findMyJob(@NotNull SeqView<Problem> allJobs) {
    // no need to synchronize because this object has been effectively immutable at this moment.
    return allJobs.filterNot(problem -> NOT_MY_JOB.contains(problem.getClass()));
  }
}
