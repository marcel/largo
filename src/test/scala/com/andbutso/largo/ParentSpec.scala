package com.andbutso.largo

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito

trait ParentSpec extends SpecificationWithJUnit with Mockito {
  args.execute(isolated = true, sequential = true)

  def identityPF[A]: PartialFunction[A, A] = { case x => x }
}
