```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "MJU Univ Auth API",
    "description": "명지대학교 학생 인증 및 정보 조회 API.",
    "version": "0.8.6"
  },
  "paths": {
    "/": {
      "get": {
        "summary": "API 상태 확인",
        "operationId": "root__get",
        "responses": {
          "200": {
            "description": "Successful Response",
            "content": {
              "application/json": {
                "schema": {

                }
              }
            }
          }
        }
      }
    },
    "/api/v1/student-basicinfo": {
      "post": {
        "summary": "학생 기본 정보 조회",
        "description": "사용자 인증 후 학적변동내역을 조회합니다.\n- **user_id**: 학번\n- **user_pw**: 비밀번호",
        "operationId": "get_student_basicinfo_api_v1_student_basicinfo_post",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/AuthRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Successful Response",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/SuccessResponse_StudentBasicInfo_"
                }
              }
            }
          },
          "401": {
            "description": "INVALID_CREDENTIALS_ERROR 인증 실패 (자격 증명 오류, 세션 만료 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "409": {
            "description": "로그인 상태에서 재 로그인등 서버 상태와 충돌 요청",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "422": {
            "description": "SERVICE_NOT_FOUND_ERROR, 처리 불가능한 요청 (잘못된 서비스 이름) 내부 로직 문제",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "500": {
            "description": "PARSING_ERROR, UNKNOWN_ERROR 서버 내부 오류 (파싱 실패 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "502": {
            "description": "NETWORK_ERROR 게이트웨이 오류 (업스트림 네트워크 문제)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/student-changelog": {
      "post": {
        "summary": "학적변동내역 조회",
        "description": "사용자 인증 후 학적변동내역을 조회합니다.\n- **user_id**: 학번\n- **user_pw**: 비밀번호",
        "operationId": "get_student_changelog_api_v1_student_changelog_post",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/AuthRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Successful Response",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/SuccessResponse_StudentChangeLog_"
                }
              }
            }
          },
          "401": {
            "description": "INVALID_CREDENTIALS_ERROR 인증 실패 (자격 증명 오류, 세션 만료 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "409": {
            "description": "로그인 상태에서 재 로그인등 서버 상태와 충돌 요청",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "422": {
            "description": "SERVICE_NOT_FOUND_ERROR, 처리 불가능한 요청 (잘못된 서비스 이름) 내부 로직 문제",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "500": {
            "description": "PARSING_ERROR, UNKNOWN_ERROR 서버 내부 오류 (파싱 실패 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "502": {
            "description": "NETWORK_ERROR 게이트웨이 오류 (업스트림 네트워크 문제)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/student-card": {
      "post": {
        "summary": "학생증 정보 조회",
        "description": "사용자 인증 후 학생증 정보를 조회합니다.\n- **user_id**: 학번\n- **user_pw**: 비밀번호",
        "operationId": "get_student_card_api_v1_student_card_post",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/AuthRequest"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Successful Response",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/SuccessResponse_StudentCard_"
                }
              }
            }
          },
          "401": {
            "description": "INVALID_CREDENTIALS_ERROR 인증 실패 (자격 증명 오류, 세션 만료 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "409": {
            "description": "로그인 상태에서 재 로그인등 서버 상태와 충돌 요청",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "422": {
            "description": "SERVICE_NOT_FOUND_ERROR, 처리 불가능한 요청 (잘못된 서비스 이름) 내부 로직 문제",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "500": {
            "description": "PARSING_ERROR, UNKNOWN_ERROR 서버 내부 오류 (파싱 실패 등)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "502": {
            "description": "NETWORK_ERROR 게이트웨이 오류 (업스트림 네트워크 문제)",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "AcademicStatus": {
        "properties": {
          "student_id": {
            "type": "string",
            "title": "Student Id",
            "default": ""
          },
          "name": {
            "type": "string",
            "title": "Name",
            "default": ""
          },
          "status": {
            "type": "string",
            "title": "Status",
            "default": ""
          },
          "grade": {
            "type": "string",
            "title": "Grade",
            "default": ""
          },
          "completed_semesters": {
            "type": "string",
            "title": "Completed Semesters",
            "default": ""
          },
          "department": {
            "type": "string",
            "title": "Department",
            "default": ""
          }
        },
        "type": "object",
        "title": "AcademicStatus",
        "description": "학적 기본 정보"
      },
      "Address": {
        "properties": {
          "postal_code": {
            "type": "string",
            "title": "Postal Code",
            "default": ""
          },
          "address": {
            "type": "string",
            "title": "Address",
            "default": ""
          }
        },
        "type": "object",
        "title": "Address",
        "description": "주소 정보"
      },
      "AuthRequest": {
        "properties": {
          "user_id": {
            "type": "string",
            "title": "User Id"
          },
          "user_pw": {
            "type": "string",
            "title": "User Pw"
          }
        },
        "type": "object",
        "required": [
          "user_id",
          "user_pw"
        ],
        "title": "AuthRequest"
      },
      "ChangeLogEntry": {
        "properties": {
          "year": {
            "type": "string",
            "title": "Year",
            "default": ""
          },
          "semester": {
            "type": "string",
            "title": "Semester",
            "default": ""
          },
          "change_type": {
            "type": "string",
            "title": "Change Type",
            "default": ""
          },
          "change_date": {
            "type": "string",
            "title": "Change Date",
            "default": ""
          },
          "expiry_date": {
            "type": "string",
            "title": "Expiry Date",
            "default": ""
          },
          "reason": {
            "type": "string",
            "title": "Reason",
            "default": ""
          }
        },
        "type": "object",
        "title": "ChangeLogEntry",
        "description": "변동 내역 리스트 항목"
      },
      "ErrorCode": {
        "type": "string",
        "enum": [
          "",
          "NETWORK_ERROR",
          "PARSING_ERROR",
          "INVALID_CREDENTIALS_ERROR",
          "SESSION_NOT_EXIST_ERROR",
          "SESSION_EXPIRED_ERROR",
          "ALREADY_LOGGED_IN_ERROR",
          "SERVICE_NOT_FOUND_ERROR",
          "SERVICE_UNKNOWN_ERROR",
          "INVALID_SERVICE_USAGE_ERROR",
          "UNKNOWN_ERROR"
        ],
        "title": "ErrorCode"
      },
      "ErrorResponse": {
        "properties": {
          "request_succeeded": {
            "type": "boolean",
            "title": "Request Succeeded"
          },
          "credentials_valid": {
            "type": "boolean",
            "title": "Credentials Valid"
          },
          "data": {
            "anyOf": [
              {

              },
              {
                "type": "null"
              }
            ],
            "title": "Data"
          },
          "error_code": {
            "type": "string",
            "title": "Error Code"
          },
          "error_message": {
            "type": "string",
            "title": "Error Message"
          },
          "success": {
            "type": "boolean",
            "title": "Success",
            "default": false
          }
        },
        "type": "object",
        "required": [
          "request_succeeded",
          "credentials_valid",
          "error_code",
          "error_message"
        ],
        "title": "ErrorResponse"
      },
      "PersonalContact": {
        "properties": {
          "english_surname": {
            "type": "string",
            "title": "English Surname",
            "default": ""
          },
          "english_givenname": {
            "type": "string",
            "title": "English Givenname",
            "default": ""
          },
          "phone_number": {
            "type": "string",
            "title": "Phone Number",
            "default": ""
          },
          "mobile_number": {
            "type": "string",
            "title": "Mobile Number",
            "default": ""
          },
          "email": {
            "type": "string",
            "title": "Email",
            "default": ""
          },
          "current_residence_address": {
            "$ref": "#/components/schemas/Address"
          },
          "resident_registration_address": {
            "$ref": "#/components/schemas/Address"
          }
        },
        "type": "object",
        "title": "PersonalContact",
        "description": "개인 연락처 정보"
      },
      "StudentBasicInfo": {
        "properties": {
          "department": {
            "type": "string",
            "title": "Department",
            "description": "소속",
            "default": ""
          },
          "category": {
            "type": "string",
            "title": "Category",
            "description": "구분",
            "default": ""
          },
          "grade": {
            "type": "string",
            "title": "Grade",
            "description": "학년",
            "default": ""
          },
          "last_access_time": {
            "type": "string",
            "title": "Last Access Time",
            "description": "최근 접속 시간",
            "default": ""
          },
          "last_access_ip": {
            "type": "string",
            "title": "Last Access Ip",
            "description": "최근 접속 IP",
            "default": ""
          },
          "raw_html_data": {
            "type": "string",
            "title": "Raw Html Data",
            "default": ""
          }
        },
        "type": "object",
        "title": "StudentBasicInfo",
        "description": "학생 기본 정보(대시보드 요약) 데이터 클래스"
      },
      "StudentCard": {
        "properties": {
          "student_profile": {
            "$ref": "#/components/schemas/StudentProfile"
          },
          "personal_contact": {
            "$ref": "#/components/schemas/PersonalContact"
          },
          "raw_html_data": {
            "type": "string",
            "title": "Raw Html Data",
            "default": ""
          }
        },
        "type": "object",
        "title": "StudentCard",
        "description": "학생카드 정보 데이터 클래스"
      },
      "StudentChangeLog": {
        "properties": {
          "academic_status": {
            "$ref": "#/components/schemas/AcademicStatus"
          },
          "cumulative_leave_semesters": {
            "type": "string",
            "title": "Cumulative Leave Semesters",
            "default": ""
          },
          "change_log_list": {
            "items": {
              "$ref": "#/components/schemas/ChangeLogEntry"
            },
            "type": "array",
            "title": "Change Log List"
          },
          "raw_html_data": {
            "type": "string",
            "title": "Raw Html Data",
            "default": ""
          }
        },
        "type": "object",
        "title": "StudentChangeLog",
        "description": "학적변동내역 정보 데이터 클래스"
      },
      "StudentProfile": {
        "properties": {
          "student_id": {
            "type": "string",
            "title": "Student Id",
            "default": ""
          },
          "name_korean": {
            "type": "string",
            "title": "Name Korean",
            "default": ""
          },
          "grade": {
            "type": "string",
            "title": "Grade",
            "default": ""
          },
          "enrollment_status": {
            "type": "string",
            "title": "Enrollment Status",
            "default": ""
          },
          "college_department": {
            "type": "string",
            "title": "College Department",
            "default": ""
          },
          "academic_advisor": {
            "type": "string",
            "title": "Academic Advisor",
            "default": ""
          },
          "student_designed_major_advisor": {
            "type": "string",
            "title": "Student Designed Major Advisor",
            "default": ""
          },
          "photo_base64": {
            "type": "string",
            "title": "Photo Base64",
            "default": ""
          }
        },
        "type": "object",
        "title": "StudentProfile",
        "description": "학생 프로필 정보"
      },
      "SuccessResponse_StudentBasicInfo_": {
        "properties": {
          "request_succeeded": {
            "type": "boolean",
            "title": "Request Succeeded",
            "default": true
          },
          "credentials_valid": {
            "type": "boolean",
            "title": "Credentials Valid",
            "default": true
          },
          "data": {
            "$ref": "#/components/schemas/StudentBasicInfo"
          },
          "error_code": {
            "anyOf": [
              {
                "$ref": "#/components/schemas/ErrorCode"
              },
              {
                "type": "null"
              }
            ]
          },
          "error_message": {
            "type": "string",
            "title": "Error Message",
            "default": ""
          },
          "success": {
            "type": "boolean",
            "title": "Success",
            "default": true
          }
        },
        "type": "object",
        "required": [
          "data"
        ],
        "title": "SuccessResponse[StudentBasicInfo]"
      },
      "SuccessResponse_StudentCard_": {
        "properties": {
          "request_succeeded": {
            "type": "boolean",
            "title": "Request Succeeded",
            "default": true
          },
          "credentials_valid": {
            "type": "boolean",
            "title": "Credentials Valid",
            "default": true
          },
          "data": {
            "$ref": "#/components/schemas/StudentCard"
          },
          "error_code": {
            "anyOf": [
              {
                "$ref": "#/components/schemas/ErrorCode"
              },
              {
                "type": "null"
              }
            ]
          },
          "error_message": {
            "type": "string",
            "title": "Error Message",
            "default": ""
          },
          "success": {
            "type": "boolean",
            "title": "Success",
            "default": true
          }
        },
        "type": "object",
        "required": [
          "data"
        ],
        "title": "SuccessResponse[StudentCard]"
      },
      "SuccessResponse_StudentChangeLog_": {
        "properties": {
          "request_succeeded": {
            "type": "boolean",
            "title": "Request Succeeded",
            "default": true
          },
          "credentials_valid": {
            "type": "boolean",
            "title": "Credentials Valid",
            "default": true
          },
          "data": {
            "$ref": "#/components/schemas/StudentChangeLog"
          },
          "error_code": {
            "anyOf": [
              {
                "$ref": "#/components/schemas/ErrorCode"
              },
              {
                "type": "null"
              }
            ]
          },
          "error_message": {
            "type": "string",
            "title": "Error Message",
            "default": ""
          },
          "success": {
            "type": "boolean",
            "title": "Success",
            "default": true
          }
        },
        "type": "object",
        "required": [
          "data"
        ],
        "title": "SuccessResponse[StudentChangeLog]"
      }
    }
  }
}
```