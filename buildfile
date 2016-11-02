require 'buildr/gpg'
require 'buildr/custom_pom'
require 'buildr/jacoco'

VERSION_NUMBER = '0.11.0-SNAPSHOT'

Release.commit_message = lambda { |version| "Bump version number to #{version}" }
Release.tag_name = lambda { |version| "v#{version}" }

SIMPLE = [:simple_common, :simple_transport, :simple_http]
UNDERTOW = [:jboss_logging, :xnio_api, :xnio, :undertow]

define 'molecule', :group => 'com.vtence.molecule', :version => VERSION_NUMBER do
  compile.options.source = '1.8'
  compile.options.target = '1.8'

  compile.with SIMPLE, UNDERTOW, :mustache
  test.with :hamcrest, :hamcrest_junit, :jmock, :juniversalchardet

  package :jar
  package :javadoc
  package :sources
  package(:test_jar).clean.path('com/vtence/molecule').include(_('target/test/classes/**/testing'))

  pom.name = 'Molecule'
  pom.description = 'A web micro-framework for Java'
  pom.add_mit_license
  pom.add_github_project('testinfected/molecule')
  pom.scm_developer_connection = 'scm:hg:git+ssh://git@github.com:testinfected/molecule.git'
  pom.add_developer('testinfected', 'Vincent Tence', 'vtence@gmail.com', ['Developer'])
  pom.optional_dependencies.concat [SIMPLE, UNDERTOW, :mustache].flatten
end
