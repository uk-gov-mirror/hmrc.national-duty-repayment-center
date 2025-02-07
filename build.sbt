import sbt.Tests.{Group, SubProcess}
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.SbtAutoBuildPlugin

lazy val compileDeps = Seq(
  ws,
  "uk.gov.hmrc"        %% "bootstrap-backend-play-26" % "3.4.0",
  "uk.gov.hmrc"        %% "auth-client"               % "3.2.0-play-26",
  "com.kenshoo"        %% "metrics-play"              % "2.6.19_0.7.0",
  "uk.gov.hmrc"        %% "domain"                    % "5.11.0-play-26",
  "com.github.blemale" %% "scaffeine"                 % "3.1.0",
  "uk.gov.hmrc"        %% "simple-reactivemongo"      % "8.0.0-play-26",
  "org.typelevel"      %% "cats-core"                 % "2.2.0",
  ws
)

def testDeps(scope: String) =
  Seq(
    "uk.gov.hmrc"            %% "hmrctest"           % "3.10.0-play-26"  % scope,
    "org.scalatest"          %% "scalatest"          % "3.0.9"          % scope,
    "org.mockito"             % "mockito-core"       % "3.1.0"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"          % scope,
    "uk.gov.hmrc"            %% "reactivemongo-test" % "5.0.0-play-26" % scope,
    "com.github.tomakehurst"  % "wiremock"           % "2.27.2"         % scope
  )

val jettyVersion = "9.2.24.v20180105"

val jettyOverrides = Seq(
  "org.eclipse.jetty"           % "jetty-server"       % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-servlet"      % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-security"     % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-servlets"     % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-continuation" % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-webapp"       % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-xml"          % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-client"       % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-http"         % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-io"           % jettyVersion % IntegrationTest,
  "org.eclipse.jetty"           % "jetty-util"         % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-api"      % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-common"   % jettyVersion % IntegrationTest,
  "org.eclipse.jetty.websocket" % "websocket-client"   % jettyVersion % IntegrationTest
)

lazy val root = (project in file("."))
  .settings(
    name := "national-duty-repayment-center",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 9380,
    resolvers := Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.bintrayRepo("hmrc", "release-candidates"),
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration",
    ScoverageKeys.coverageMinimum := 46.43,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    libraryDependencies ++= compileDeps ++ testDeps("test") ++ testDeps("it"),
    dependencyOverrides ++= jettyOverrides,
    publishingSettings,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    routesImport ++= Seq("uk.gov.hmrc.nationaldutyrepaymentcenter.binders.UrlBinders._")
  )
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    majorVersion := 0
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
