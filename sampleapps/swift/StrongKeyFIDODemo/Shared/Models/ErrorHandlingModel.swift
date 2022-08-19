//
//  ErrorHandlingModel.swift
//  StrongKeyFIDODemo
//
//  Created by Strongkey Engg on 10/21/21.
//

import SwiftUI

enum NetworkError: Error {
    case transportError(Error)
    case serverError(statusCode: Int)
    case noData
    case decodingError(Error)
    case encodingError(Error)
    case backendError(message: String)
}

extension NetworkError {
    
    init?(data: Data?, response: URLResponse?, error: Error?) {
        if let error = error {
            self = .transportError(error)
            return
        }

        if let response = response as? HTTPURLResponse,
            !(200...299).contains(response.statusCode) {
            do {
                let errorResponseModel = try JSONDecoder().decode(ErrorResponseModel.self, from: data!)
                self = .backendError(message: errorResponseModel.message)
                return
            } catch {
                self = .serverError(statusCode: response.statusCode)
                return
            }
        }
        
        if data == nil {
            self = .noData
        }
        
        return nil
    }
}
