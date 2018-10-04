package di

import com.google.inject.AbstractModule
import repo.{AcroDb, PosesAndTransistionsRepo}

class AcroDIModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[AcroDb]).toInstance(AcroDb.devDb)
    bind(classOf[PosesAndTransistionsRepo]).asEagerSingleton()
  }
}
