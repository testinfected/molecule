# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

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
- `ApacheLogger`s will now correctly log request parameters as they were received, in case they are modified down the middleware chain

[0.10]: https://github.com/testinfected/molecule/compare/v0.10...v0.9.1
[0.9.1]: https://github.com/testinfected/molecule/compare/v0.9.1...v0.9

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
[#4]: https://github.com/testinfected/molecule/issues/4
