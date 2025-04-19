<template>
  <div class="user-page">
    <div class="background"></div> <!-- 背景层 -->
    <div class="user-content">
      <h2>欢迎来到用户界面</h2>
      <button @click="logout">退出登录</button>
      <router-link to="/patient-manage">进入患者管理</router-link>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    logout() {
      // 退出登录后返回到登录界面
      this.$router.push({ name: "login" });
    }
  }
};
</script>

<style scoped>
.user-page {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}
.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/fig1.png') no-repeat center center;
  background-size: cover;
  opacity: 0.5; /* 设置透明度 */
  z-index: 1;
}
.user-content {
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.8); /* 背景半透明，避免文字看不清 */
  padding: 40px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
button {
  padding: 10px 20px;
  margin: 10px;
  cursor: pointer;
}
a {
  margin-top: 10px;
  text-decoration: none;
  color: #007bff;
}
</style>
