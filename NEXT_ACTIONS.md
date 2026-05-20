# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 19/56 (33.9%)
- **Function parity:** 140/892 matched (target 230) — 15.7%
- **Class/type parity:** 87/462 matched (target 124) — 18.8%
- **Combined symbol parity:** 227/1354 matched (target 354) — 16.8%
- **Average inline-code cosine:** 0.39 (function body across 19 matched files)
- **Average documentation cosine:** 0.66 (doc text across 19 matched files)
- **Cheat-zeroed Files:** 5
- **Critical Issues:** 13 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. model

- **Target:** `model.Model [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2002010.0
- **Functions:** 9/95 matched (target 12)
- **Missing functions:** `object`, `default`, `fmt`, `serialize`, `deserialize`, `into_json_value`, `schema_name`, `json_schema`, `extensions`, `extensions_mut`, `with_param`, `request`, `response`, `error`, `notification`, `into_request`, `into_response`, `into_notification`, `into_error`, `into_result`, `from`, `result_as`, `params_as`, `meta`, `meta_mut`, `from_build_env`, `auto`, `required`, `none`, `into_vec`, `is_empty`, `len`, `first`, `iter`, `as_text`, `as_tool_use`, `as_tool_result`, `text`, `tool_use`, `tool_result`, `new_multiple`, `user_text`, `assistant_text`, `user_tool_result`, `assistant_tool_use`, `try_from`, `task`, `task_mut`, `validate`, `validate_tool_use_result_balance`, `with_arguments`, `get_argument`, `has_arguments`, `argument_names`, `with_all_values`, `with_pagination`, `has_more_results`, `total_available`, `for_prompt`, `for_resource`, `reference_type`, `as_prompt_name`, `as_resource_uri`, `success`, `structured`, `structured_error`, `into_typed`, `method`, `empty`, `try_into`, `test_notification_serde`, `test_custom_client_notification_roundtrip`, `test_custom_server_notification_roundtrip`, `test_custom_request_roundtrip`, `test_request_conversion`, `test_initial_request_response_serde`, `test_negative_and_large_request_ids`, `test_protocol_version_order`, `test_icon_serialization`, `test_icon_minimal`, `test_implementation_with_icons`, `test_backward_compatibility`, `test_initialize_with_icons`, `test_elicitation_deserialization_untagged`, `test_elicitation_deserialization`, `test_elicitation_serialization`
- **Types:** 13/125 matched (target 16)
- **Missing types:** `Request`, `RequestOptionalParam`, `RequestNoParam`, `Notification`, `NotificationNoParam`, `JsonRpcRequest`, `DefaultResponse`, `JsonRpcResponse`, `JsonRpcError`, `JsonRpcNotification`, `JsonRpcMessage`, `EmptyResult`, `CustomResult`, `CancelledNotificationParam`, `CancelledNotification`, `CustomNotification`, `CustomRequest`, `InitializeRequest`, `InitializedNotification`, `InitializeRequestParams`, `InitializeRequestParam`, `InitializeResult`, `ServerInfo`, `ClientInfo`, `Implementation`, `PaginatedRequestParams`, `PaginatedRequestParam`, `PingRequest`, `ProgressNotificationParam`, `ProgressNotification`, `Cursor`, `ListResourcesRequest`, `ListResourceTemplatesRequest`, `ReadResourceRequestParams`, `ReadResourceRequestParam`, `ReadResourceResult`, `ReadResourceRequest`, `ResourceListChangedNotification`, `SubscribeRequestParams`, `SubscribeRequestParam`, `SubscribeRequest`, `UnsubscribeRequestParams`, `UnsubscribeRequestParam`, `UnsubscribeRequest`, `ResourceUpdatedNotificationParam`, `ResourceUpdatedNotification`, `ListPromptsRequest`, `GetPromptRequestParams`, `GetPromptRequestParam`, `GetPromptRequest`, `PromptListChangedNotification`, `ToolListChangedNotification`, `LoggingLevel`, `SetLevelRequestParams`, `SetLevelRequestParam`, `SetLevelRequest`, `LoggingMessageNotificationParam`, `LoggingMessageNotification`, `CreateMessageRequest`, `ToolChoiceMode`, `ToolChoice`, `SamplingContent`, `SamplingMessage`, `SamplingMessageContent`, `Error`, `ContextInclusion`, `CreateMessageRequestParams`, `CreateMessageRequestParam`, `ModelPreferences`, `ModelHint`, `CompletionContext`, `CompleteRequestParams`, `CompleteRequestParam`, `CompleteRequest`, `CompletionInfo`, `CompleteResult`, `Reference`, `ResourceReference`, `PromptReference`, `ArgumentInfo`, `Root`, `ListRootsRequest`, `ListRootsResult`, `RootsListChangedNotification`, `ElicitationAction`, `CreateElicitationRequestParamDeserializeHelper`, `CreateElicitationRequestParams`, `CreateElicitationRequestParam`, `CreateElicitationResult`, `CreateElicitationRequest`, `ElicitationResponseNotificationParam`, `ElicitationCompletionNotification`, `CallToolResult`, `CallToolResultHelper`, `ListToolsRequest`, `CallToolRequestParams`, `CallToolRequestParam`, `CallToolRequest`, `CreateMessageResult`, `GetPromptResult`, `GetTaskInfoRequest`, `GetTaskInfoParams`, `GetTaskInfoParam`, `ListTasksRequest`, `GetTaskResultRequest`, `GetTaskResultParams`, `GetTaskResultParam`, `CancelTaskRequest`, `CancelTaskParams`, `CancelTaskParam`, `GetTaskInfoResult`, `ListTasksResult`
- **Tests:** 0/16 matched

### 2. server.prompt

- **Target:** `model.PromptTest [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 191910.0
- **Functions:** 0/7 matched (target 3)
- **Missing functions:** `new`, `as_request_context`, `as_request_context_mut`, `into_get_prompt_result`, `poll`, `from_context_part`, `cached_arguments_from_schema`
- **Types:** 0/12 matched (target 1)
- **Missing types:** `PromptContext`, `GetPromptHandler`, `DynGetPromptHandler`, `AsyncMethodAdapter`, `AsyncMethodWithArgsAdapter`, `AsyncPromptAdapter`, `SyncPromptAdapter`, `AsyncPromptMethodAdapter`, `SyncPromptMethodAdapter`, `IntoGetPromptResult`, `Output`, `PromptName`
- **Provenance warning:** port-lint provenance header matched only by basename: `model/prompt.rs` vs expected `handler/server/prompt.rs`
- **Proposed provenance header:** `// port-lint: source handler/server/prompt.rs` (current: `// port-lint: source model/prompt.rs`)
- **Lint issues:** 1

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

### 5. model.meta

- **Target:** `model.Meta`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 82207.0
- **Functions:** 9/16 matched (target 14)
- **Missing functions:** `extensions`, `extensions_mut`, `get_meta_mut`, `get_meta`, `deref`, `deref_mut`, `insert_extension`
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

### 9. model.resource

- **Target:** `model.Resource`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 41108.1
- **Functions:** 2/6 matched (target 2)
- **Missing functions:** `test_resource_serialization`, `test_resource_contents_serialization`, `test_resource_template_with_icons`, `test_resource_template_without_icons`
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 10. model.prompt

- **Target:** `model.Prompt`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 31603.9
- **Functions:** 8/11 matched (target 12)
- **Missing functions:** `test_prompt_message_image_serialization`, `test_prompt_message_resource_link_serialization`, `test_prompt_message_content_resource_link_deserialization`
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 11. model.capabilities

- **Target:** `model.Capabilities`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 4004.2
- **Functions:** 23/23 matched (target 58)
- **Missing functions:** _none_
- **Types:** 17/17 matched (target 20)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 12. model.content

- **Target:** `model.Content [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 3210.0
- **Functions:** 18/18 matched (target 31)
- **Missing functions:** _none_
- **Types:** 14/14 matched (target 21)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 13. model.tool

- **Target:** `model.Tool`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 1903.2
- **Functions:** 15/15 matched (target 17)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_

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

### 15. model.task

- **Target:** `model.Task`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 500.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 16. transport.io

- **Target:** `transport.Io`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 105.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 17. server.resource

- **Target:** `model.ResourceTest [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `model/resource.rs` vs expected `handler/server/resource.rs`
- **Proposed provenance header:** `// port-lint: source handler/server/resource.rs` (current: `// port-lint: source model/resource.rs`)
- **Lint issues:** 1

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

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/rmcp/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/rmcp kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

