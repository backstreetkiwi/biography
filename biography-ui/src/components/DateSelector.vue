<template>
<div>
  <div>
    <ul class="ds">
      <li class="item item-year" v-bind:class="{'open' : dropDowns.year.open}" v-on:click="toggleYearDropDown()">
        <a class="link" href="#">{{yearMenuCaption}}</a>
        <ul class="dropdown">
          <li v-for="year in years" v-on:click="yearSelected(year.year)">
            <a class="link" href="#">{{year.year}}</a>
          </li>
        </ul>
      </li>  
      <li class="item item-month" v-bind:class="{'open' : dropDowns.month.open}" v-on:click="toggleMonthDropDown()">
        <a class="link" href="#">{{monthMenuCaption}}</a>
        <ul class="dropdown">
          <li v-for="month in months" v-on:click="monthSelected(month.yearMonth)">
            <a class="link" href="#">{{month.caption}}</a>
          </li>
        </ul>
      </li>  
      <li class="item item-day" v-bind:class="{'open' : dropDowns.day.open}" v-on:click="toggleDayDropDown()">
        <a class="link" href="#">{{dayMenuCaption}}</a>
        <ul class="dropdown">
          <li v-for="day in days" v-on:click="daySelected(day.date)">
            <a class="link" href="#">{{day.caption}}</a>  
          </li>
        </ul>
      </li>  
    </ul>
  </div>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "DateSelector",
  data() {
    return {
      selectedYear: null,
      selectedMonth: null,
      selectedDate: null,
      yearMenuCaption: "year",
      monthMenuCaption: "month",
      dayMenuCaption: "day",
      years: [],
      months: [],
      days: [],
      dropDowns: {
        year: { open: false },
        month: { open: false },
        day: { open: false },
      },
      monthNames: {
        "01" : "January",
        "02" : "February",
        "03" : "March",
        "04" : "April",
        "05" : "May",
        "06" : "June",
        "07" : "July",
        "08" : "August",
        "09" : "September",
        "10" : "October",
        "11" : "November",
        "12" : "December"
      }
    };
  },
  mounted() {
      this.updateYears();
  },
  methods: {
    updateYears: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" }).then(result => {
            this.years = result.data;
        }, error => {
            this.years = [];
        });
    },
    updateMonths: function() {                                    
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" + this.selectedYear + "/" }).then(result => {
            for (var key in result.data) {
                result.data[key].caption = this.monthName(result.data[key].yearMonth);
            }
            this.months = result.data;
        }, error => {
            this.months = [];
        });
    },
    updateDays: function() {                                    
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" + this.selectedYear + "/" + this.selectedMonth + "/" }).then(result => {
            for (var key in result.data) {
                result.data[key].caption = result.data[key].date.slice(-2);
            }
            this.days = result.data;
        }, error => {
            this.days = [];
        });
    },
    yearSelected: function(newYear) {
      this.selectedYear = newYear;
      this.updateMonths();
      this.days = [];
      this.yearMenuCaption = newYear;
      this.monthMenuCaption = "month";
      this.dayMenuCaption = "day";
      this.$emit("dayChanged", null);
    },
    monthSelected: function(newYearMonth) {
      this.selectedMonth = newYearMonth.slice(-2);
      this.updateDays();
      this.monthMenuCaption = this.monthName(newYearMonth);
      this.dayMenuCaption = "day";
      this.$emit("dayChanged", null);
    },
    daySelected: function(newDate) {
      this.selectedDate = newDate;
      this.dayMenuCaption = newDate.slice(-2);
      this.$emit("dayChanged", this.selectedDate);
    },
    toggleYearDropDown: function() {
      this.dropDowns.year.open = !this.dropDowns.year.open;
    },
    toggleMonthDropDown: function() {
      this.dropDowns.month.open = !this.dropDowns.month.open;
    },
    toggleDayDropDown: function() {
      this.dropDowns.day.open = !this.dropDowns.day.open;
    },
    monthName: function(yearMonth) {
      var month = yearMonth.slice(-2);
      return this.monthNames[month];
    }
  }
};
</script>

<style scoped>
.ds {
  list-style: none;
  display: flex;
}

.item {
  position: relative;
	padding-right: 3rem; 
  text-align: left;
}

.item-year {
  width: 30px;
}

.item-month {
  width: 70px;
}

.item-day {
  width: 30px;
}

.link {
  text-transform: uppercase;
  text-decoration: none;
  color: brown;
}

.open .dropdown {
  display: block;
}

.dropdown {
	display: none;
  list-style: none;
	position: absolute;
  padding: 2px;
	font-size: 0.9rem;
	top: 2.2rem;
	box-shadow: 0px 6px 12px rgba(0, 0, 0, 0.2);
	border-radius: 4px;
  background-color: aqua;
}

.dropdown li {
  padding: 5px;
}

.dropdown li a {
  padding: 5px;
}






</style>