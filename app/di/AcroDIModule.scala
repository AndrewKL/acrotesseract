package di

import com.google.inject.AbstractModule
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait, TestRepo}

class AcroDIModule extends AbstractModule {
  override def configure(): Unit = {

    bind(classOf[PosesAndTransitionsTrait]).toInstance(new TestRepo)
  }
}
