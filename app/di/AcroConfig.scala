package di

case class AcroConfig ( domainName:String,
                        oauthRedirectUrl:String,
                        acrotesseractDevJDBC:String,
                        dbPasswordSecretName:String,
                        googleAuthSecretName:String
                      )

object AcroConfig{
  val dev = AcroConfig (
    domainName = "localhost:9000",
    oauthRedirectUrl = "http://localhost:9000/oauth2callback",
    acrotesseractDevJDBC = "jdbc:mysql://acrotesseract-db.cgccqt70jhl5.us-west-2.rds.amazonaws.com:3306/dev",
    dbPasswordSecretName = "acrotesseract-rds-username-pw",
    googleAuthSecretName = "acrotesseract-google-auth"
  )
}
