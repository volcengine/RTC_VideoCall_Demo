#pragma once
#include <map>
#include <mutex>
#include <string>
#include <vector>

namespace vrd {

struct VerifySms {
  // 用户ID
  std::string user_id;
  // 用户名
  std::string user_name;
  // 登录鉴权字段
  std::string login_token;
  // 创建时间点
  int64_t create_at = 0;
};

struct RTSInfo {
	// 每个应用的唯一标识符
	std::string app_id;
	// appID鉴权字段
	std::string rtm_token;
	// 服务端URL
	std::string server_url;
	// 服务端鉴权签名
	std::string server_signature;
};

#define PROPRETY(CLASS, MEMBER, UPPER_MEMBER)   \
 private:                                       \
  CLASS MEMBER##_;                              \
                                                \
 public:                                        \
  CLASS MEMBER() const {                        \
    std::lock_guard<std::mutex> _(_mutex);      \
    return MEMBER##_;                           \
  }                                             \
  void set##UPPER_MEMBER(const CLASS& MEMBER) { \
    std::lock_guard<std::mutex> _(_mutex);      \
    MEMBER##_ = MEMBER;                         \
  }                                             \
  void set##UPPER_MEMBER(CLASS&& MEMBER) {      \
    std::lock_guard<std::mutex> _(_mutex);      \
    MEMBER##_ = std::move(MEMBER);              \
  }

class DataMgr {
public:
	static DataMgr& instance() {
		static DataMgr data_mgr;
		return data_mgr;
	}

	PROPRETY(VerifySms, verify_sms, VerifySms)
	PROPRETY(std::string, login_token, LoginToken)
	PROPRETY(std::string, user_name, UserName)
	PROPRETY(std::string, user_id, UserId)
	PROPRETY(std::string, room_id, RoomId)
	PROPRETY(std::string, business_Id, BusinessId)
	PROPRETY(RTSInfo, rts_info, RTSInfo)
	PROPRETY(std::vector<std::string>, scenes_list, ScenesList)

protected:
	DataMgr() = default;
	~DataMgr() = default;

private:
	mutable std::mutex _mutex;
};

#undef PROPRETY
}  // namespace vrd
