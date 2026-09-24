import SwiftUI

struct ErrorContent: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text("⚠️")
                .font(.system(size: 40))
            Text(message)
                .foregroundStyle(.red)
                .multilineTextAlignment(.center)
            Button("Reload", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding(24)
    }
}

#Preview {
    ErrorContent(message: "Unable to reach the weather service", onRetry: {})
}
