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

[0.10]: https://github.com/testinfected/molecule/compare/v0.10...v0.9.1

[#48]: https://github.com/testinfected/molecule/issues/48
[#47]: https://github.com/testinfected/molecule/issues/47
[#44]: https://github.com/testinfected/molecule/issues/44
[#41]: https://github.com/testinfected/molecule/issues/41
[#4]: https://github.com/testinfected/molecule/issues/4
