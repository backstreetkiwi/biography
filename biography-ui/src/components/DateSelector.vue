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
      yearMenuCaption: "year",
      monthMenuCaption: "month",
      years: [],
      months: [],
      dropDowns: {
        year: { open: false },
        month: { open: false },
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
    yearSelected: function(newYear) {
      this.selectedYear = newYear;
      this.updateMonths();
      this.days = [];
      this.yearMenuCaption = newYear;
      this.monthMenuCaption = "month";
      this.$emit("monthChanged", null);
    },
    monthSelected: function(newYearMonth) {
      this.selectedMonth = newYearMonth.slice(-2);
      this.monthMenuCaption = this.monthName(newYearMonth);
      this.$emit("monthChanged", this.selectedYear + "-" + this.selectedMonth);
    },
    toggleYearDropDown: function() {
      this.dropDowns.year.open = !this.dropDowns.year.open;
    },
    toggleMonthDropDown: function() {
      this.dropDowns.month.open = !this.dropDowns.month.open;
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
  z-index: 1000;
}

.dropdown li {
  padding: 5px;
}

.dropdown li a {
  padding: 5px;
}






</style>