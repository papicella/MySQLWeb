document.addEventListener('DOMContentLoaded', function () {
    const toggle = document.querySelector('.navbar-toggle')
    const collapse = document.querySelector('.navbar-collapse')
    const dropdowns = document.querySelectorAll('.dropdown')

    if (toggle && collapse) {
        toggle.addEventListener('click', function () {
            collapse.classList.toggle('open')
        })
    }

    dropdowns.forEach(function (dropdown) {
        const toggleLink = dropdown.querySelector('.dropdown-toggle')
        if (!toggleLink) {
            return
        }

        toggleLink.addEventListener('click', function (event) {
            if (window.innerWidth > 768) {
                return
            }

            event.preventDefault()
            dropdown.classList.toggle('open')
        })
    })
})
