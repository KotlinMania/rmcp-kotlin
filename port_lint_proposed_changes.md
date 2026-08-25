# port-lint Proposed Changes

**Generated:** 2026-08-25
**Source:** tmp/rmcp/src
**Target:** src/commonMain/kotlin

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonTest/kotlin/io/github/kotlinmania/rmcp/handler/server/CommonTest.kt` | `// port-lint: source handler/server/common.rs` | `// port-lint: source transport/common.rs` | `transport/common.rs` | `port-lint provenance header matched only by basename: 'handler/server/common.rs' vs expected 'transport/common.rs'` |
