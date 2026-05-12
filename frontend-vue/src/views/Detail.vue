<template>
  <div class="detail-page">
    <TicketForm :eventId="id" />
  </div>
</template>

<script>
import TicketForm from '../components/TicketForm.vue'
export default {
  name: 'DetailView',
  components: { TicketForm },
  props: { id: { type: [String, Number], required: true } }
}
</script>