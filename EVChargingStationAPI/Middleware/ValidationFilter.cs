// ========================================
// Middleware/ValidationFilter.cs
// ========================================
/*
 * ValidationFilter.cs
 * Handle 500 status errors
 * Date: September 2025
 * Description: Avoid revealing too much info with bad requests.
 */

using EVChargingStationAPI.Models.DTOs;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace EVChargingStationAPI.Middleware
{
    /// <summary>
    /// Action filter that validates incoming model state before the controller action executes.
    /// Converts validation errors into a clean <see cref="ApiResponseDTO{T}"/> 
    /// instead of returning ASP.NET Core's default validation error payload.
    /// </summary>
    public class ValidationFilter : IActionFilter
    {
        /// <summary>
        /// Runs before the action method executes.
        /// If the <see cref="ActionExecutingContext.ModelState"/> is invalid, 
        /// it returns a <see cref="BadRequestObjectResult"/> with a standardized API response.
        /// </summary>
        /// <param name="context">The context for the current action execution.</param>
        public void OnActionExecuting(ActionExecutingContext context)
        {
            if (!context.ModelState.IsValid)
            {
                var errors = context.ModelState
                    .Where(x => x.Value != null && x.Value.Errors != null && x.Value.Errors.Count > 0)
                    .Select(x =>
                    {
                        var error = x.Value?.Errors?.FirstOrDefault();
                        var errorMessage = error?.ErrorMessage ?? "Unknown error";
                        return $"{x.Key}: {errorMessage}";
                    })
                    .ToList();

                var response = new ApiResponseDTO<List<string>>
                {
                    Success = false,
                    Message = "Validation failed",
                    Data = errors
                };

                context.Result = new BadRequestObjectResult(response);
            }
        }

        /// <summary>
        /// Runs after the action method has executed.
        /// Currently unused, but required by <see cref="IActionFilter"/>.
        /// </summary>
        /// <param name="context">The context for the executed action.</param>
        public void OnActionExecuted(ActionExecutedContext context) { }
    }
}
