<!-- eslint-disable vue/multi-word-component-names -->

<template>
  <div class="login">
    <div class="background"></div>
    <div class="login-content">
      <h2>登录</h2>
      <input
        v-model="form.username"
        type="text"
        placeholder="请输入用户名"
        @keyup.enter="handleLogin"
      />
      <input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        @keyup.enter="handleLogin"
      />
      <button @click="handleLogin" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
//import { mapActions } from 'vuex'; // 如果使用Vuex管理登录状态

export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      errorMessage: ''
    };
  },
  methods: {
    async handleLogin() {
      // 1. 前端验证
      if (!this.form.username || !this.form.password) {
        this.errorMessage = '用户名和密码不能为空';
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        // 2. 调用后端API
        const response = await axios.post('/api/login', this.form);

        // 3. 处理登录成功
        localStorage.setItem('token', response.data.token); // 存储token
        //this.$store.commit('setUser', response.data.user); // Vuex存储用户信息

        // 4. 跳转到目标页面
        this.$router.push("/user")

      } catch (error) {
        // 5. 错误处理
        this.errorMessage = error.response?.data?.message ||
                          error.message ||
                          '登录失败，请重试';
        console.error('登录错误:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.error {
  color: red;
  margin-top: 10px;
}
button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>

<style scoped>
.login {
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
  opacity: 0.5; /* 设置透明度为50% */
  z-index: 1;
}
.login-content {
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.8);
  padding: 40px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
input {
  padding: 10px;
  margin: 10px;
  width: 200px;
}
button {
  padding: 10px 20px;
  cursor: pointer;
}
</style>
