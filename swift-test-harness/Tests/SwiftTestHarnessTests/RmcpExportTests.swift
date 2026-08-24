import Testing
import Rmcp

@Suite("Rmcp Swift Export Tests")
struct RmcpExportTests {
    @Test("Swift module loads cleanly")
    func testSwiftModuleLoads() {
        #expect(Bool(true))
    }

    @Test("Exported model types are accessible")
    func testExportedTypes() {
        let userRole = model.Role.User
        #expect(userRole == .User)
    }
}

