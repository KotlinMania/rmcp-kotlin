# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 18/94 (19.1%)
- **Function parity:** 323/1184 matched (target 563) — 27.3%
- **Class/type parity:** 220/537 matched (target 359) — 41.0%
- **Combined symbol parity:** 543/1721 matched (target 922) — 31.6%
- **Average inline-code cosine:** 0.56 (function body across 18 matched files)
- **Average documentation cosine:** 0.80 (doc text across 18 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 11 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **wrapper.json** (13 deps)
   - Path: `rmcp/src/handler/server/wrapper/json.rs`
   - Essential for 13 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. rmcp.error

- **Target:** `rmcp.Error`
- **Similarity:** 0.45
- **Dependents:** 8
- **Priority Score:** 8000405.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_

### 2. rmcp.model

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

### 4. model.elicitation_schema

- **Target:** `model.ElicitationSchema [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 91610.0
- **Functions:** 88/91 matched (target 123)
- **Missing functions:** `from_type`, `default_untitled_multi_select`, `test_schema_inference_for_enum_fields`
- **Types:** 20/25 matched (target 42)
- **Missing types:** `SingleSelect`, `MultiSelect`, `TitledEnum`, `UntitledEnum`, `UserInfo`
- **Tests:** 24/26 matched

### 5. model.meta

- **Target:** `model.Meta`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 72206.6
- **Functions:** 10/16 matched (target 15)
- **Missing functions:** `extensions`, `get_meta_mut`, `get_meta`, `deref`, `deref_mut`, `insert_extension`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Target`

### 6. common.client_side_sse

- **Target:** `common.ClientSideSse`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 51804.9
- **Functions:** 6/8 matched (target 9)
- **Missing functions:** `default`, `poll_next`
- **Types:** 7/10 matched (target 14)
- **Missing types:** `Error`, `Future`, `Item`

### 7. common.server_side_http

- **Target:** `common.ServerSideHttp`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 51307.1
- **Functions:** 6/9 matched (target 6)
- **Missing functions:** `poll`, `from_duration`, `reset`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `BoxResponse`, `Output`

### 8. model.annotated

- **Target:** `model.Annotated`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 42003.3
- **Functions:** 13/15 matched (target 17)
- **Missing functions:** `deref`, `deref_mut`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Target`, `Sealed`

### 9. server.common

- **Target:** `server.Common`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 11805.7
- **Functions:** 11/12 matched (target 13)
- **Missing functions:** `from_context_part`
- **Types:** 6/6 matched (target 7)
- **Missing types:** _none_
- **Tests:** 9/9 matched

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

### 18. common.http_header

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

