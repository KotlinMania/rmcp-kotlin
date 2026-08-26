# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 19/56 (33.9%)
- **Function parity:** 314/866 matched (target 563) — 36.3%
- **Class/type parity:** 218/462 matched (target 359) — 47.2%
- **Combined symbol parity:** 532/1328 matched (target 922) — 40.1%
- **Average inline-code cosine:** 0.51 (function body across 19 matched files)
- **Average documentation cosine:** 0.76 (doc text across 19 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 12 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `rmcp.Error`
- **Similarity:** 0.45
- **Dependents:** 8
- **Priority Score:** 8000405.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_

### 2. model

- **Target:** `model.Model`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 152005.2
- **Functions:** 85/95 matched (target 211)
- **Missing functions:** `object`, `fmt`, `schema_name`, `json_schema`, `extensions`, `meta_mut`, `try_from`, `task_mut`, `try_into`, `test_request_conversion`
- **Types:** 122/125 matched (target 199)
- **Missing types:** `Error`, `CreateElicitationRequestParamDeserializeHelper`, `CallToolResultHelper`
- **Tests:** 15/16 matched

### 3. model.extension

- **Target:** `model.Extension`
- **Similarity:** 0.24
- **Dependents:** 0
- **Priority Score:** 122707.6
- **Functions:** 13/22 matched (target 16)
- **Missing functions:** `write`, `write_u64`, `finish`, `fmt`, `clone_box`, `as_any`, `as_any_mut`, `into_any`, `clone`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `AnyMap`, `IdHasher`, `AnyClone`
- **Tests:** 1/1 matched

### 4. server.common

- **Target:** `server.Common`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 121809.6
- **Functions:** 2/12 matched (target 4)
- **Missing functions:** `from_context_part`, `test_schema_for_type_handles_primitive`, `test_schema_for_type_handles_array`, `test_schema_for_type_handles_struct`, `test_schema_for_type_caches_primitive_types`, `test_schema_for_type_caches_struct_types`, `test_schema_for_type_different_types_different_schemas`, `test_schema_for_type_arc_can_be_shared`, `test_schema_for_output_rejects_primitive`, `test_schema_for_output_accepts_object`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `TestObject`, `AnotherTestObject`
- **Tests:** 0/9 matched

### 5. model.elicitation_schema

- **Target:** `model.ElicitationSchema [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 91610.0
- **Functions:** 88/91 matched (target 123)
- **Missing functions:** `from_type`, `default_untitled_multi_select`, `test_schema_inference_for_enum_fields`
- **Types:** 20/25 matched (target 42)
- **Missing types:** `SingleSelect`, `MultiSelect`, `TitledEnum`, `UntitledEnum`, `UserInfo`
- **Tests:** 24/26 matched

### 6. model.meta

- **Target:** `model.Meta`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 72206.6
- **Functions:** 10/16 matched (target 15)
- **Missing functions:** `extensions`, `get_meta_mut`, `get_meta`, `deref`, `deref_mut`, `insert_extension`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Target`

### 7. common.client_side_sse

- **Target:** `common.ClientSideSse`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 51804.9
- **Functions:** 6/8 matched (target 9)
- **Missing functions:** `default`, `poll_next`
- **Types:** 7/10 matched (target 14)
- **Missing types:** `Error`, `Future`, `Item`

### 8. common.server_side_http

- **Target:** `common.ServerSideHttp`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 51307.1
- **Functions:** 6/9 matched (target 6)
- **Missing functions:** `poll`, `from_duration`, `reset`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `BoxResponse`, `Output`

### 9. model.annotated

- **Target:** `model.Annotated`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 42003.3
- **Functions:** 13/15 matched (target 17)
- **Missing functions:** `deref`, `deref_mut`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Target`, `Sealed`

### 10. model.capabilities

- **Target:** `model.Capabilities`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 4004.2
- **Functions:** 23/23 matched (target 58)
- **Missing functions:** _none_
- **Types:** 17/17 matched (target 20)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 11. model.content

- **Target:** `model.Content`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 3202.6
- **Functions:** 18/18 matched (target 35)
- **Missing functions:** _none_
- **Types:** 14/14 matched (target 21)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 12. model.tool

- **Target:** `model.Tool`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 1903.2
- **Functions:** 15/15 matched (target 17)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_

### 13. model.prompt

- **Target:** `model.Prompt`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 1601.9
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 11)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 14. server.tool_name_validation

- **Target:** `server.ToolNameValidation`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 1601.7
- **Functions:** 15/15 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 11/11 matched

### 15. model.resource

- **Target:** `model.Resource`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 1104.1
- **Functions:** 6/6 matched (target 8)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 16. model.task

- **Target:** `model.Task`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 500.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 17. transport.io

- **Target:** `transport.Io`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 105.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 18. transport.common

- **Target:** `server.CommonTest [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `handler/server/common.rs` vs expected `transport/common.rs`
- **Proposed provenance header:** `// port-lint: source transport/common.rs` (current: `// port-lint: source handler/server/common.rs`)
- **Lint issues:** 1

### 19. common.http_header

- **Target:** `common.HttpHeader`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

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

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

