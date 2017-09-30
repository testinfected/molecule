# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.13.0] - 2017-09-30

This version introduces a major breaking change in the API. It implements a more functional
programming style. 

### Added
- Simplified request URI handling with a new `Uri` class to manipulate and deconstruct URIs. 
  It is immutable and replaces individual URI components in `Request`. ([#67])

### Changed
- `Application` are now simple functions of `Request -> Response`. Middlewares become simple functions of 
  `Application -> Application`. ([#64])
- Request `uri` is now the full URI, reconstructed from server host and port. ([#67])
- `HttpStatus` is now a class rather than an enum, which means custom HTTP statuses are supported. ([#66]) 

### Fixed
- URL Map middleware was failing to dispatch to root mount. `/foo` is now matched if mounted to `/`). ([#62])

## [0.12.0] - 2017-09-21

### Added
- A middleware to force SSL connections. It does permanent redirects and adds the HSTS header. ([#36])

### Changed
- Session cookies are now encrypted using SHA256 instead of SHA1 by default. ([#60])
- Session cookies are now encoded in RFC4648 base64. ([#61])

### Fixed
- URL Map middleware was failing to dispatch to root mount. `/foo` is now matched if mounted to `/`). ([#62])

## [0.11.0] - 2016-11-17

### Added
- A basic authentication middleware with pluggable authentication providers. ([#28])
- A server adapter for powering Molecule with [Undertow](http://undertow.io). Undertow is fast! ([#53])
- The possibility to check for the presence of a given request parameter. 
This avoids checks against null for boolean parameters. See `Request#hasParameter`. ([#49])
- The request query string. See `Request#query`. ([#54])
- The server host name and the request host name, the latter taken from the _HOST_ header. 
  See `Request#serverHost` and `Request#hostname`. ([#55])
- The server port and the request port, the latter taken from the _HOST_ header. 
  See `Request#serverPort` and `Request#port`. ([#56])
- The request scheme. See `Request#scheme`. ([#58])
- The reconstructed request URL. See `Request#url`. ([#57])
 
### Changed
- The `testing` package, which contains helpers for testing applications built with Molecule, is now included in the main jar. 
The test jar is no longer distributed. ([#51])
- Request input streams are now closed automatically at the end of the request cycle. This includes file uploads. ([#52])
 
### Fixed
- The test HTTP client no longer loses the _Content-Type_ header when creating a fresh request from
 a prototype request. ([#50])
- ETag middleware now properly closes original body after computing ETag.

## [0.10] - 2016-06-28
### Added
- Add a cookie session storage mechanism, as an alternative to the in-memory session pool.
Sessions stored on the client include a secure digest of the content to prevent against session forgery. ([#4])
- Cookie session storage supports secret key rotation. ([#48]) 
- Add an hex decoder that decodes hexadecimal representations to their bytes form. 
`HexEncoder` does encoding and decoding to/from hex representations. ([#47])
- Add a flash hash as a way of passing messages through redirection. 
Anything in the flash is exposed to the very next request and then cleared out. ([#44])
- Session pool can now renew a session id whenever the session changes. 
This helps prevent from session fixation attacks. ([#41])

### Changed
- `SessionIdentifierPolicy` now receives the session data to support more complex use cases of session id generation

### Fixed
- `ConditionalGet` middleware no longer throws an exception when _Modified-Since_ header has unsupported format

## [0.9.1] - 2016-01-11

### Added

- It is now possible to replace the session bound to the request by a fresh new session to avoid session fixation attacks. ([#43])
- Session pool now sweeps sessions that have exceeded the maximum lifetime. The maximum lifetime is configurable. ([#42])
This helps prevent sessions from being maintained and kept alive forever.
- Session pool can now renew a session id whenever the session changes. This helps prevent from session fixation attacks. ([#41])
- Session pool now sweeps stale sessions.
Sessions are considered stale when they have been inactive for longer than the configurable idle timeout. ([#40])
- It is now possible to boot the application with a warm-up sequence
- Add an `URLMap` middleware for dispatching requests to different apps based on the request URI. ([#38])
- Add support for logging in Apache Combine Format to logger middleware ([@ensonik](https://github.com/ensonik) in [#37])

### Changed
- Write multiple cookie values as distinct Set-Cookie headers instead of single one - as per rfc6265 recommendation. ([@gbranchaudrubenovitch](https://github.com/gbranchaudrubenovitch) in [#46])
- Session keys are automatically converted to their string representations
- Default session cookie name is now _molecule.session_

### Removed
- `PlainErrorReporter` is no longer provided - you have to write your own reporters

### Fixed
- `MiddlewareStack` no longer mixes up middlewares and mount points when several mount points are defined

## [0.9] - 2015-09-14

### Added

- It is now possible to serve content asynchronously in a separate thread to the original servicing thread. ([#35])
- Filters can now be set using custom `RequestMatcher`s

### Fixed
- `ApacheCommonLogger` now correctly logs request parameters as they were received, in case they are modified down the middleware chain


[0.12.0]: https://github.com/testinfected/molecule/compare/v0.11.0...master
[0.11.0]: https://github.com/testinfected/molecule/compare/v0.10...v0.11.1
[0.10]: https://github.com/testinfected/molecule/compare/v0.9.1...v0.10
[0.9.1]: https://github.com/testinfected/molecule/compare/v0.9...v0.9.1
[0.9]: https://github.com/testinfected/molecule/compare/v0.8.2...v0.9

[#62]: https://github.com/testinfected/molecule/issues/62
[#61]: https://github.com/testinfected/molecule/issues/61
[#60]: https://github.com/testinfected/molecule/issues/60
[#58]: https://github.com/testinfected/molecule/issues/58
[#57]: https://github.com/testinfected/molecule/issues/57
[#56]: https://github.com/testinfected/molecule/issues/56
[#55]: https://github.com/testinfected/molecule/issues/55
[#54]: https://github.com/testinfected/molecule/issues/54
[#53]: https://github.com/testinfected/molecule/issues/53
[#52]: https://github.com/testinfected/molecule/issues/52
[#51]: https://github.com/testinfected/molecule/issues/51
[#50]: https://github.com/testinfected/molecule/issues/50
[#49]: https://github.com/testinfected/molecule/issues/49
[#48]: https://github.com/testinfected/molecule/issues/48
[#47]: https://github.com/testinfected/molecule/issues/47
[#46]: https://github.com/testinfected/molecule/issues/46
[#44]: https://github.com/testinfected/molecule/issues/44
[#43]: https://github.com/testinfected/molecule/issues/43
[#42]: https://github.com/testinfected/molecule/issues/42
[#41]: https://github.com/testinfected/molecule/issues/41
[#40]: https://github.com/testinfected/molecule/issues/40
[#38]: https://github.com/testinfected/molecule/issues/38
[#37]: https://github.com/testinfected/molecule/issues/37
[#36]: https://github.com/testinfected/molecule/issues/36
[#35]: https://github.com/testinfected/molecule/issues/35
[#28]: https://github.com/testinfected/molecule/issues/28
[#4]: https://github.com/testinfected/molecule/issues/4
