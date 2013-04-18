define 'molecule', :group => 'com.vtence.molecule', :version => '0.2-SNAPSHOT' do
  compile.options.source = '1.7'
  compile.options.target = '1.7'

  compile.with :simpleweb
  test.with :hamcrest, :jmock, :cglib, :objenesis, :jmock_legacy, :juniversalchardet
  test.with transitive(artifacts(:htmlunit))
  package :jar

  package_with_javadoc
  package_with_sources
end
