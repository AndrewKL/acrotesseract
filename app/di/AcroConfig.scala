package di

case class AcroConfig (domainName:String,
                       oauthRedirectUrl:String,
                       jdbcString:String,
                       dbPasswordSecretName:String,
                       googleAuthSecretName:String,
                       profileCredentialsName:Option[String]
                      )

object AcroConfig {
  val dev = AcroConfig (
    domainName = "localhost:9000",
    oauthRedirectUrl = "http://localhost:9000/oauth2callback",
    jdbcString = "jdbc:mysql://acrotesseract-db.cgccqt70jhl5.us-west-2.rds.amazonaws.com:3306/dev",
    dbPasswordSecretName = "acrotesseract-rds-username-pw",
    googleAuthSecretName = "acrotesseract-google-auth",
    profileCredentialsName = Option("acrotesseract")
  )

  val prod = AcroConfig (
    domainName = "www.acrotesseract.info",
    oauthRedirectUrl = "http://www.acrotesseract.info/oauth2callback",
    jdbcString = "jdbc:mysql://acrotesseract-db.cgccqt70jhl5.us-west-2.rds.amazonaws.com:3306/prod",
    dbPasswordSecretName = "acrotesseract-rds-username-pw",
    googleAuthSecretName = "acrotesseract-google-auth",
    profileCredentialsName = Option.empty
  )
}
