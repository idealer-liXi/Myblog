<template>
  <Transition name="confirm-modal">
    <div v-if="visible" class="confirm-modal-overlay" @click.self="handleCancel">
      <div class="confirm-modal-box">
        <div class="confirm-modal-icon">
          <i class="bi bi-volume-up"></i>
        </div>
        <h3 class="confirm-modal-title">播放音乐</h3>
        <p class="confirm-modal-text">即将播放音乐，确认继续吗？</p>
        <p class="confirm-modal-hint">后续将不再提示</p>
        <div class="confirm-modal-actions">
          <button class="confirm-modal-btn confirm-modal-cancel" @click="handleCancel">取消</button>
          <button class="confirm-modal-btn confirm-modal-ok" @click="handleOk">确认播放</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
let resolveFn = null

const show = () => {
  return new Promise((resolve) => {
    resolveFn = resolve
    visible.value = true
  })
}

const handleOk = () => {
  visible.value = false
  if (resolveFn) {
    resolveFn(true)
    resolveFn = null
  }
}

const handleCancel = () => {
  visible.value = false
  if (resolveFn) {
    resolveFn(false)
    resolveFn = null
  }
}

defineExpose({ show })
</script>

<style scoped>
.confirm-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.confirm-modal-box {
  width: min(90vw, 360px);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px 28px 24px;
  text-align: center;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22), inset 0 0 0 1px rgba(255, 255, 255, 0.65);
  animation: confirm-modal-in 0.28s ease-out;
}

@keyframes confirm-modal-in {
  from { opacity: 0; transform: scale(0.92) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.confirm-modal-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: #334155;
  box-shadow: 0 4px 14px rgba(168, 237, 234, 0.35);
}

.confirm-modal-title {
  margin: 0 0 8px;
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b;
}

.confirm-modal-text {
  margin: 0 0 4px;
  font-size: 0.92rem;
  color: #475569;
  line-height: 1.5;
}

.confirm-modal-hint {
  margin: 0 0 20px;
  font-size: 0.78rem;
  color: #94a3b8;
}

.confirm-modal-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-modal-btn {
  border: none;
  border-radius: 12px;
  padding: 10px 22px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s ease;
}

.confirm-modal-cancel {
  background: rgba(148, 163, 184, 0.15);
  color: #64748b;
}

.confirm-modal-cancel:hover {
  background: rgba(148, 163, 184, 0.28);
}

.confirm-modal-ok {
  background: linear-gradient(135deg, #4a86e8, #7c9cff);
  color: #fff;
  box-shadow: 0 4px 14px rgba(74, 134, 232, 0.35);
}

.confirm-modal-ok:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 134, 232, 0.45);
}

.confirm-modal-enter-active {
  transition: opacity 0.25s ease;
}

.confirm-modal-leave-active {
  transition: opacity 0.2s ease;
}

.confirm-modal-enter-from,
.confirm-modal-leave-to {
  opacity: 0;
}
</style>