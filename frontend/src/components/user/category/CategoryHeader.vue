<template>
  <div class="flex justify-end items-center mb-4 gap-4">
    <RouterLink to="/category-regist" class="text-sm text-gray-600 hover:text-gray-800 flex items-center gap-1">
      <PlusCircleIcon class="w-7 h-7" />
    </RouterLink>
    <RouterLink to="/category-manage" class="text-sm text-gray-600 hover:text-gray-800 flex items-center gap-1">
      <Cog6ToothIcon class="w-7 h-7" />
    </RouterLink>
  </div>
</template>

<script setup>
import { Cog6ToothIcon, PlusCircleIcon } from "@heroicons/vue/24/outline";
</script>
