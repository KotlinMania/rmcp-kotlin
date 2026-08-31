# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 18/56 (32.1%)
- **Function parity:** 235/775 matched (target 440) — 30.3%
- **Class/type parity:** 200/437 matched (target 317) — 45.8%
- **Combined symbol parity:** 435/1212 matched (target 757) — 35.9%
- **Average inline-code cosine:** 0.56 (function body across 18 matched files)
- **Average documentation cosine:** 0.80 (doc text across 18 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 11 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `rmcp.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 8
- **Priority Score:** 8000405.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/error.rs` vs expected `error.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/error.rs` vs expected `error.rs`
- **Proposed provenance header:** `// port-lint: source error.rs` (current: `// port-lint: source rmcp/src/error.rs`)
- **Proposed provenance header:** `// port-lint: tests error.rs` (current: `// port-lint: tests rmcp/src/error.rs`)
- **Lint issues:** 2

### 2. model

- **Target:** `model.Model [PROVENANCE-FALLBACK]`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 152005.2
- **Functions:** 85/95 matched (target 211)
- **Missing functions:** `object`, `fmt`, `schema_name`, `json_schema`, `extensions`, `meta_mut`, `try_from`, `task_mut`, `try_into`, `test_request_conversion`
- **Types:** 122/125 matched (target 199)
- **Missing types:** `Error`, `CreateElicitationRequestParamDeserializeHelper`, `CallToolResultHelper`
- **Tests:** 15/16 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model.rs` vs expected `model.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model.rs` vs expected `model.rs`
- **Proposed provenance header:** `// port-lint: source model.rs` (current: `// port-lint: source rmcp/src/model.rs`)
- **Proposed provenance header:** `// port-lint: tests model.rs` (current: `// port-lint: tests rmcp/src/model.rs`)
- **Lint issues:** 2

### 3. model.extension

- **Target:** `model.Extension [PROVENANCE-FALLBACK]`
- **Similarity:** 0.24
- **Dependents:** 0
- **Priority Score:** 122707.6
- **Functions:** 13/22 matched (target 16)
- **Missing functions:** `write`, `write_u64`, `finish`, `fmt`, `clone_box`, `as_any`, `as_any_mut`, `into_any`, `clone`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `AnyMap`, `IdHasher`, `AnyClone`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/extension.rs` vs expected `model/extension.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model/extension.rs` vs expected `model/extension.rs`
- **Proposed provenance header:** `// port-lint: source model/extension.rs` (current: `// port-lint: source rmcp/src/model/extension.rs`)
- **Proposed provenance header:** `// port-lint: tests model/extension.rs` (current: `// port-lint: tests rmcp/src/model/extension.rs`)
- **Lint issues:** 2

### 4. model.meta

- **Target:** `model.Meta [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 72206.6
- **Functions:** 10/16 matched (target 15)
- **Missing functions:** `extensions`, `get_meta_mut`, `get_meta`, `deref`, `deref_mut`, `insert_extension`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/meta.rs` vs expected `model/meta.rs`
- **Proposed provenance header:** `// port-lint: source model/meta.rs` (current: `// port-lint: source rmcp/src/model/meta.rs`)
- **Lint issues:** 1

### 5. common.client_side_sse

- **Target:** `common.ClientSideSse [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 51804.9
- **Functions:** 6/8 matched (target 9)
- **Missing functions:** `default`, `poll_next`
- **Types:** 7/10 matched (target 14)
- **Missing types:** `Error`, `Future`, `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/transport/common/client_side_sse.rs` vs expected `transport/common/client_side_sse.rs`
- **Proposed provenance header:** `// port-lint: source transport/common/client_side_sse.rs` (current: `// port-lint: source rmcp/src/transport/common/client_side_sse.rs`)
- **Lint issues:** 1

### 6. common.server_side_http

- **Target:** `common.ServerSideHttp [PROVENANCE-FALLBACK]`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 51307.1
- **Functions:** 6/9 matched (target 6)
- **Missing functions:** `poll`, `from_duration`, `reset`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `BoxResponse`, `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/transport/common/server_side_http.rs` vs expected `transport/common/server_side_http.rs`
- **Proposed provenance header:** `// port-lint: source transport/common/server_side_http.rs` (current: `// port-lint: source rmcp/src/transport/common/server_side_http.rs`)
- **Lint issues:** 1

### 7. model.annotated

- **Target:** `model.Annotated [PROVENANCE-FALLBACK]`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 42003.3
- **Functions:** 13/15 matched (target 17)
- **Missing functions:** `deref`, `deref_mut`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Target`, `Sealed`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/annotated.rs` vs expected `model/annotated.rs`
- **Proposed provenance header:** `// port-lint: source model/annotated.rs` (current: `// port-lint: source rmcp/src/model/annotated.rs`)
- **Lint issues:** 1

### 8. server.common

- **Target:** `server.Common [PROVENANCE-FALLBACK]`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 11805.7
- **Functions:** 11/12 matched (target 13)
- **Missing functions:** `from_context_part`
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/handler/server/common.rs` vs expected `handler/server/common.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/handler/server/common.rs` vs expected `handler/server/common.rs`
- **Proposed provenance header:** `// port-lint: source handler/server/common.rs` (current: `// port-lint: source rmcp/src/handler/server/common.rs`)
- **Proposed provenance header:** `// port-lint: tests handler/server/common.rs` (current: `// port-lint: tests rmcp/src/handler/server/common.rs`)
- **Lint issues:** 2

### 9. model.capabilities

- **Target:** `model.Capabilities [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 4004.2
- **Functions:** 23/23 matched (target 58)
- **Missing functions:** _none_
- **Types:** 17/17 matched (target 20)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/capabilities.rs` vs expected `model/capabilities.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model/capabilities.rs` vs expected `model/capabilities.rs`
- **Proposed provenance header:** `// port-lint: source model/capabilities.rs` (current: `// port-lint: source rmcp/src/model/capabilities.rs`)
- **Proposed provenance header:** `// port-lint: tests model/capabilities.rs` (current: `// port-lint: tests rmcp/src/model/capabilities.rs`)
- **Lint issues:** 2

### 10. model.content

- **Target:** `model.Content [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 3202.6
- **Functions:** 18/18 matched (target 35)
- **Missing functions:** _none_
- **Types:** 14/14 matched (target 21)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/content.rs` vs expected `model/content.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model/content.rs` vs expected `model/content.rs`
- **Proposed provenance header:** `// port-lint: source model/content.rs` (current: `// port-lint: source rmcp/src/model/content.rs`)
- **Proposed provenance header:** `// port-lint: tests model/content.rs` (current: `// port-lint: tests rmcp/src/model/content.rs`)
- **Lint issues:** 2

### 11. model.tool

- **Target:** `model.Tool [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 1903.2
- **Functions:** 15/15 matched (target 17)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/tool.rs` vs expected `model/tool.rs`
- **Proposed provenance header:** `// port-lint: source model/tool.rs` (current: `// port-lint: source rmcp/src/model/tool.rs`)
- **Lint issues:** 1

### 12. model.prompt

- **Target:** `model.Prompt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 1601.9
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 11)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/prompt.rs` vs expected `model/prompt.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model/prompt.rs` vs expected `model/prompt.rs`
- **Proposed provenance header:** `// port-lint: source model/prompt.rs` (current: `// port-lint: source rmcp/src/model/prompt.rs`)
- **Proposed provenance header:** `// port-lint: tests model/prompt.rs` (current: `// port-lint: tests rmcp/src/model/prompt.rs`)
- **Lint issues:** 2

### 13. server.tool_name_validation

- **Target:** `server.ToolNameValidation [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 1601.7
- **Functions:** 15/15 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 11/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/handler/server/tool_name_validation.rs` vs expected `handler/server/tool_name_validation.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/handler/server/tool_name_validation.rs` vs expected `handler/server/tool_name_validation.rs`
- **Proposed provenance header:** `// port-lint: source handler/server/tool_name_validation.rs` (current: `// port-lint: source rmcp/src/handler/server/tool_name_validation.rs`)
- **Proposed provenance header:** `// port-lint: tests handler/server/tool_name_validation.rs` (current: `// port-lint: tests rmcp/src/handler/server/tool_name_validation.rs`)
- **Lint issues:** 2

### 14. model.resource

- **Target:** `model.Resource [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 1104.1
- **Functions:** 6/6 matched (target 8)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/resource.rs` vs expected `model/resource.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:rmcp/src/model/resource.rs` vs expected `model/resource.rs`
- **Proposed provenance header:** `// port-lint: source model/resource.rs` (current: `// port-lint: source rmcp/src/model/resource.rs`)
- **Proposed provenance header:** `// port-lint: tests model/resource.rs` (current: `// port-lint: tests rmcp/src/model/resource.rs`)
- **Lint issues:** 2

### 15. model.task

- **Target:** `model.Task [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 500.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/model/task.rs` vs expected `model/task.rs`
- **Proposed provenance header:** `// port-lint: source model/task.rs` (current: `// port-lint: source rmcp/src/model/task.rs`)
- **Lint issues:** 1

### 16. transport.io

- **Target:** `transport.Io [PROVENANCE-FALLBACK]`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 105.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/transport/io.rs` vs expected `transport/io.rs`
- **Proposed provenance header:** `// port-lint: source transport/io.rs` (current: `// port-lint: source rmcp/src/transport/io.rs`)
- **Lint issues:** 1

### 17. common.http_header

- **Target:** `common.HttpHeader [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `rmcp/src/transport/common/http_header.rs` vs expected `transport/common/http_header.rs`
- **Proposed provenance header:** `// port-lint: source transport/common/http_header.rs` (current: `// port-lint: source rmcp/src/transport/common/http_header.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Matched

| Source | Target | Path |
|--------|--------|------|
| `model.elicitation_schema` | `model.ElicitationSchema` | `model/elicitation_schema` |

