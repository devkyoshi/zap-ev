import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { ShieldX } from "lucide-react"

type ErrorPageProps = {
  title?: string
  message?: string
  code?: number
}

const ErrorPage = ({
  title = "Unauthorized Access",
  message = "You don't have permission to access this resource.",
  code = 401,
}: ErrorPageProps) => {
  const navigate = useNavigate()
  
  return (
    <div className="flex min-h-screen flex-col items-center justify-center text-center">
      <div className="mx-auto flex w-full max-w-lg flex-col items-center space-y-6 px-4 py-8">
        <div className="flex h-24 w-24 items-center justify-center rounded-full bg-destructive/10 text-destructive">
          <ShieldX size={48} />
        </div>
        
        <div className="space-y-2">
          <h1 className="text-4xl font-bold tracking-tighter sm:text-5xl">
            {code}
          </h1>
          <h2 className="text-2xl font-semibold tracking-tight">
            {title}
          </h2>
          <p className="max-w-md text-muted-foreground">
            {message}
          </p>
        </div>
        
        <div className="flex flex-col sm:flex-row gap-2">
          <Button
            variant="outline"
            onClick={() => navigate(-1)}
          >
            Go Back
          </Button>
          <Button onClick={() => navigate("/auth/login")}>
            Return to Login
          </Button>
        </div>
      </div>
    </div>
  )
}

export default ErrorPage
