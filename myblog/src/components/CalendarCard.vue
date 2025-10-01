<template>
  <div class="calendar-card">
    <div class="calendar-header">
      <div class="calendar-title">
        <i class="bi bi-calendar-check"></i>
        <span>{{ currentYear }} 年 {{ currentMonth + 1 }} 月</span>
      </div>
      <div class="calendar-nav">
        <button @click="prevMonth" class="nav-btn">&lt;</button>
        <button @click="nextMonth" class="nav-btn">&gt;</button>
      </div>
    </div>
    <div class="calendar-grid">
      <div class="day-header" v-for="day in weekDays" :key="day">{{ day }}</div>
      <div class="day-cell" v-for="day in calendarDays" :key="day.date" :class="{ 'today': day.isToday, 'other-month': !day.isCurrentMonth }">
        {{ day.day }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
const today = new Date();
const currentYear = ref(today.getFullYear());
const currentMonth = ref(today.getMonth());

const calendarDays = computed(() => {
  const year = currentYear.value;
  const month = currentMonth.value;
  const firstDayOfMonth = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const days = [];

  // Previous month's days
  const prevMonthDays = new Date(year, month, 0).getDate();
  for (let i = firstDayOfMonth; i > 0; i--) {
    days.push({
      date: new Date(year, month - 1, prevMonthDays - i + 1),
      day: prevMonthDays - i + 1,
      isCurrentMonth: false,
    });
  }

  // Current month's days
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i);
    days.push({
      date,
      day: i,
      isCurrentMonth: true,
      isToday: date.toDateString() === today.toDateString(),
    });
  }

  // Next month's days
  const remainingCells = 42 - days.length; // 6 rows * 7 days
  for (let i = 1; i <= remainingCells; i++) {
    days.push({
      date: new Date(year, month + 1, i),
      day: i,
      isCurrentMonth: false,
    });
  }

  return days;
});

const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
};

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
};
</script>

<style scoped>
.calendar-card {
  background: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.07);
  margin-top: 20px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.calendar-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.calendar-title i {
  margin-right: 6px;
  color: #4facfe;
  font-size: 0.9em;
}

.calendar-nav .nav-btn {
  background: none;
  border: 1px solid #e0e0e0;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  margin-left: 4px;
  color: #777;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.calendar-nav .nav-btn:hover {
  background-color: #f5f5f5;
  border-color: #bbb;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  text-align: center;
}

.day-header {
  font-weight: 500;
  font-size: 0.75rem;
  color: #888;
  padding-bottom: 8px;
}

.day-cell {
  font-size: 0.75rem;
  padding: 4px 0;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.day-cell.other-month {
  color: #d0d0d0;
}

.day-cell.today {
  background-color: #4facfe;
  color: #fff;
  font-weight: 500;
}
</style>
