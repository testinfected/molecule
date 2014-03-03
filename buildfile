VERSION_NUMBER = "0.3-SNAPSHOT"
define 'molecule', :group => 'com.vtence.molecule', :version => VERSION_NUMBER do
  compile.options.source = '1.7'
  compile.options.target = '1.7'

  compile.with :simple
  test.with :hamcrest, :jmock, :cglib, :objenesis, :jmock_legacy, :juniversalchardet
  test.with transitive(artifacts(:htmlunit))

  package :jar
  package :sources
  package :javadoc
  package :test_jar

  pom.name = 'Molecule'
  pom.description = 'A Rack inspired web micro-framework for Java'
  pom.add_mit_license
  pom.add_github_project('testinfected', 'molecule')
  pom.add_developer('testinfected', 'Vincent Tence', 'vtence@gmail.com', ['Developer'])
  pom.optional_dependencies.concat [:simple]

  repositories.release_to = {
    :url => 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
    :username => Buildr.settings.user['sonatype']['username'],
    :password => Buildr.settings.user['sonatype']['password']
  }
  repositories.snapshot_to = {
    :url => 'https://oss.sonatype.org/content/repositories/snapshots/',
    :username => Buildr.settings.user['sonatype']['username'],
    :password => Buildr.settings.user['sonatype']['password']
  }
end
